"""ShortKey — 단축키로 앱 실행 (Windows)"""
import os
import sys
import json
import socket
import threading
import subprocess
import ctypes
import tkinter as tk
from tkinter import ttk, filedialog, messagebox

import keyboard

_MUTEX = None   # GC에 의해 해제되지 않도록 모듈 수준에서 유지
_IPC_PORT = 47891


def _resource(name):
    """PyInstaller 번들 내부 경로와 개발 환경 경로를 모두 처리"""
    base = getattr(sys, '_MEIPASS', os.path.dirname(os.path.abspath(__file__)))
    return os.path.join(base, name)


def _acquire_single_instance():
    """이미 실행 중이면 해당 인스턴스에 설정 창 열기 신호를 보내고 False 반환"""
    global _MUTEX
    _MUTEX = ctypes.windll.kernel32.CreateMutexW(None, False, "ShortKey_SingleInstance")
    if ctypes.windll.kernel32.GetLastError() == 183:  # ERROR_ALREADY_EXISTS
        try:
            s = socket.create_connection(('127.0.0.1', _IPC_PORT), timeout=1)
            s.close()
        except Exception:
            pass
        return False
    return True

try:
    import pystray
    from PIL import Image, ImageDraw
    HAS_TRAY = True
except ImportError:
    HAS_TRAY = False

CONFIG = os.path.join(os.path.expanduser('~'), '.shortkey.json')

_MODS = {
    'ctrl', 'shift', 'alt', 'windows',
    'left ctrl', 'right ctrl',
    'left shift', 'right shift',
    'left alt', 'right alt',
    'left windows', 'right windows',
}
_ALIASES = {
    'left ctrl': 'ctrl', 'right ctrl': 'ctrl',
    'left shift': 'shift', 'right shift': 'shift',
    'left alt': 'alt', 'right alt': 'alt',
    'left windows': 'windows', 'right windows': 'windows',
}
_MOD_ORDER = ['ctrl', 'shift', 'alt', 'windows']


def _fmt(keys):
    n = {_ALIASES.get(k.lower(), k.lower()) for k in keys}
    ordered = [m for m in _MOD_ORDER if m in n] + sorted(n - set(_MOD_ORDER))
    return '+'.join(ordered)


class HotkeyCapture:
    """단축키 조합을 한 번 캡처하는 클래스"""

    def __init__(self):
        self._keys = set()
        self._result = None
        self._done = threading.Event()
        self._hook = None

    def start(self):
        self._hook = keyboard.hook(self._handle, suppress=True)

    def cancel(self):
        self._stop()
        self._done.set()

    @property
    def done(self):
        return self._done.is_set()

    @property
    def result(self):
        return self._result

    def current(self):
        return _fmt(self._keys) if self._keys else ''

    def _stop(self):
        if self._hook:
            try:
                keyboard.unhook(self._hook)
            except Exception:
                pass
            self._hook = None

    def _handle(self, event):
        k = event.name.lower()
        if event.event_type == keyboard.KEY_DOWN:
            if k in ('esc', 'escape'):
                self._stop()
                self._done.set()
            else:
                self._keys.add(k)
        elif event.event_type == keyboard.KEY_UP and k not in ('esc', 'escape'):
            if self._keys - _MODS:
                self._result = _fmt(self._keys)
                self._stop()
                self._done.set()
            self._keys.discard(k)


class ShortcutDialog(tk.Toplevel):
    """단축키 추가/수정 다이얼로그"""

    def __init__(self, parent, sc=None):
        super().__init__(parent)
        self.title('단축키 추가' if sc is None else '단축키 수정')
        self.geometry('490x260')
        self.resizable(False, False)
        self.transient(parent)
        self.grab_set()
        self.result = None
        self._cap = None
        self._rec = False
        self._build(sc or {})
        self.protocol('WM_DELETE_WINDOW', self._cancel)
        self.focus_force()

    def _build(self, sc):
        f = ttk.Frame(self, padding=20)
        f.pack(fill=tk.BOTH, expand=True)

        ttk.Label(f, text='앱 이름:').grid(row=0, column=0, sticky='w', pady=8, padx=(0, 10))
        self._name = tk.StringVar(value=sc.get('name', ''))
        ttk.Entry(f, textvariable=self._name, width=38).grid(row=0, column=1, columnspan=2, sticky='ew')

        ttk.Label(f, text='앱 경로 / 명령어:').grid(row=1, column=0, sticky='w', pady=8, padx=(0, 10))
        self._path = tk.StringVar(value=sc.get('path', ''))
        ttk.Entry(f, textvariable=self._path, width=30).grid(row=1, column=1, sticky='ew')
        ttk.Button(f, text='찾기...', command=self._browse, width=7).grid(row=1, column=2, padx=(5, 0))

        ttk.Label(f, text='.exe .lnk 파일 또는 명령어 직접 입력 (notepad, calc, ms-settings: 등)',
                  foreground='#888', font=('', 8)).grid(row=2, column=1, columnspan=2, sticky='w')

        ttk.Label(f, text='단축키:').grid(row=3, column=0, sticky='w', pady=8, padx=(0, 10))
        hf = ttk.Frame(f)
        hf.grid(row=3, column=1, columnspan=2, sticky='ew')
        self._hotkey = tk.StringVar(value=sc.get('hotkey', ''))
        ttk.Entry(hf, textvariable=self._hotkey, width=22, state='readonly').pack(side=tk.LEFT)
        self._rbtn = ttk.Button(hf, text='단축키 녹음', command=self._toggle, width=12)
        self._rbtn.pack(side=tk.LEFT, padx=(8, 0))

        self._status = tk.StringVar(value="'단축키 녹음' 버튼을 누른 후 원하는 키 조합을 입력하세요")
        ttk.Label(f, textvariable=self._status, foreground='gray', font=('', 8)).grid(
            row=4, column=0, columnspan=3, sticky='w', pady=(2, 0))

        bf = ttk.Frame(f)
        bf.grid(row=5, column=0, columnspan=3, pady=(14, 0))
        ttk.Button(bf, text='확인', command=self._ok, width=12).pack(side=tk.LEFT, padx=5)
        ttk.Button(bf, text='취소', command=self._cancel, width=12).pack(side=tk.LEFT)
        f.columnconfigure(1, weight=1)

    def _browse(self):
        p = filedialog.askopenfilename(
            parent=self, title='앱 선택',
            filetypes=[
                ('실행 파일 및 바로가기', '*.exe *.lnk'),
                ('실행 파일', '*.exe'),
                ('바로가기 (*.lnk)', '*.lnk'),
                ('모든 파일', '*.*'),
            ])
        if p:
            self._path.set(p.replace('/', '\\'))
            if not self._name.get():
                self._name.set(os.path.splitext(os.path.basename(p))[0])

    def _toggle(self):
        if self._rec:
            self._stop_rec()
        else:
            self._start_rec()

    def _start_rec(self):
        self._rec = True
        self._rbtn.config(text='취소')
        self._hotkey.set('')
        self._status.set('키 조합을 누르세요... (ESC로 취소)')
        self._cap = HotkeyCapture()
        self._cap.start()
        self.after(50, self._poll)

    def _poll(self):
        if not self._rec:
            return
        if self._cap.done:
            r = self._cap.result
            self._rec = False
            self._rbtn.config(text='단축키 녹음')
            if r:
                self._hotkey.set(r)
                self._status.set(f'단축키 설정됨: {r}')
            else:
                self._hotkey.set('')
                self._status.set('취소됨')
        else:
            c = self._cap.current()
            if c:
                self._hotkey.set(c + ' ...')
            self.after(50, self._poll)

    def _stop_rec(self):
        if self._cap:
            self._cap.cancel()
        self._rec = False
        self._rbtn.config(text='단축키 녹음')
        self._status.set('취소됨')

    def _ok(self):
        name = self._name.get().strip()
        path = self._path.get().strip()
        hotkey = self._hotkey.get().strip().rstrip(' .')
        if not name:
            messagebox.showwarning('경고', '앱 이름을 입력해 주세요.', parent=self)
            return
        if not path:
            messagebox.showwarning('경고', '앱 경로를 선택해 주세요.', parent=self)
            return
        if not hotkey:
            messagebox.showwarning('경고', '단축키를 설정해 주세요.', parent=self)
            return
        self.result = {'name': name, 'path': path, 'hotkey': hotkey}
        self.destroy()

    def _cancel(self):
        if self._rec:
            self._stop_rec()
        self.destroy()


class SettingsWindow:
    """단축키 목록 관리 창"""

    def __init__(self, root, app):
        self.app = app
        self.w = tk.Toplevel(root)
        self.w.title('ShortKey 설정')
        self.w.geometry('660x420')
        self.w.minsize(500, 300)
        self.w.protocol('WM_DELETE_WINDOW', self.hide)
        self._build()
        self.refresh()

    def _build(self):
        top = ttk.Frame(self.w, padding=(15, 12, 15, 8))
        top.pack(fill=tk.X)
        ttk.Label(top, text='ShortKey', font=('', 13, 'bold')).pack(side=tk.LEFT)
        ttk.Label(top, text='  단축키로 앱을 실행합니다', foreground='#666').pack(side=tk.LEFT, pady=2)
        ttk.Separator(self.w).pack(fill=tk.X, padx=15)

        lf = ttk.Frame(self.w, padding=(15, 8))
        lf.pack(fill=tk.BOTH, expand=True)

        cols = ('name', 'hotkey', 'path')
        self.tree = ttk.Treeview(lf, columns=cols, show='headings', selectmode='browse')
        self.tree.heading('name', text='앱 이름')
        self.tree.heading('hotkey', text='단축키')
        self.tree.heading('path', text='앱 경로')
        self.tree.column('name', width=140, minwidth=80)
        self.tree.column('hotkey', width=180, minwidth=100)
        self.tree.column('path', width=300, minwidth=150)
        sb = ttk.Scrollbar(lf, command=self.tree.yview)
        self.tree.configure(yscrollcommand=sb.set)
        self.tree.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        sb.pack(side=tk.RIGHT, fill=tk.Y)
        self.tree.bind('<Double-Button-1>', lambda _: self._edit())

        bf = ttk.Frame(self.w, padding=(15, 0, 15, 12))
        bf.pack(fill=tk.X)
        ttk.Button(bf, text='+ 추가', command=self._add, width=10).pack(side=tk.LEFT, padx=(0, 4))
        ttk.Button(bf, text='수정', command=self._edit, width=10).pack(side=tk.LEFT, padx=4)
        ttk.Button(bf, text='삭제', command=self._del, width=10).pack(side=tk.LEFT, padx=4)
        ttk.Button(bf, text='완전 종료', command=self._quit_app, width=10).pack(side=tk.RIGHT)

    def refresh(self):
        self.tree.delete(*self.tree.get_children())
        for i, s in enumerate(self.app.shortcuts):
            self.tree.insert('', 'end', iid=str(i),
                             values=(s.get('name', ''), s.get('hotkey', ''), s.get('path', '')))

    def show(self):
        self.w.deiconify()
        self.w.lift()
        self.w.focus_force()

    def hide(self):
        self.w.withdraw()

    def _selected(self):
        sel = self.tree.selection()
        return int(sel[0]) if sel else None

    def _add(self):
        d = ShortcutDialog(self.w)
        self.w.wait_window(d)
        if d.result:
            self.app.shortcuts.append(d.result)
            self.app.save()
            self.app.register_all()
            self.refresh()

    def _edit(self):
        idx = self._selected()
        if idx is None:
            messagebox.showinfo('알림', '수정할 항목을 선택해 주세요.', parent=self.w)
            return
        d = ShortcutDialog(self.w, self.app.shortcuts[idx])
        self.w.wait_window(d)
        if d.result:
            self.app.shortcuts[idx] = d.result
            self.app.save()
            self.app.register_all()
            self.refresh()

    def _del(self):
        idx = self._selected()
        if idx is None:
            messagebox.showinfo('알림', '삭제할 항목을 선택해 주세요.', parent=self.w)
            return
        name = self.app.shortcuts[idx].get('name', '')
        if messagebox.askyesno('삭제', f"'{name}' 단축키를 삭제할까요?", parent=self.w):
            self.app.shortcuts.pop(idx)
            self.app.save()
            self.app.register_all()
            self.refresh()

    def _quit_app(self):
        self.app._quit()


class App:
    def __init__(self):
        self.shortcuts = []
        self.root = tk.Tk()
        self.root.withdraw()
        self._win = None
        self._tray = None

        self._load()
        self.register_all()
        self._start_ipc_server()
        if HAS_TRAY:
            self._start_tray()
        if not self.shortcuts or not HAS_TRAY:
            self.root.after(100, self._open_settings)

    def _load(self):
        try:
            if os.path.exists(CONFIG):
                with open(CONFIG, encoding='utf-8') as f:
                    self.shortcuts = json.load(f).get('shortcuts', [])
        except Exception:
            self.shortcuts = []

    def save(self):
        try:
            with open(CONFIG, 'w', encoding='utf-8') as f:
                json.dump({'shortcuts': self.shortcuts}, f, ensure_ascii=False, indent=2)
        except Exception as e:
            messagebox.showerror('오류', f'설정 저장 실패:\n{e}')

    def register_all(self):
        try:
            keyboard.unhook_all_hotkeys()
        except Exception:
            pass
        for sc in self.shortcuts:
            h, p = sc.get('hotkey', ''), sc.get('path', '')
            if h and p:
                try:
                    keyboard.add_hotkey(h, lambda path=p: self._launch(path))
                except Exception as e:
                    print(f'[ShortKey] 등록 실패: {h} — {e}')

    def _launch(self, path):
        # 포그라운드 창 강탈 방지 해제 → 실행한 앱이 맨 앞으로 올라옴
        ctypes.windll.user32.AllowSetForegroundWindow(0xFFFFFFFF)
        try:
            ret = ctypes.windll.shell32.ShellExecuteW(None, "open", path, None, None, 1)
            if ret <= 32:
                raise OSError(f"ShellExecute error: {ret}")
        except Exception:
            try:
                subprocess.Popen(path, shell=True)
            except Exception as e:
                print(f'[ShortKey] launch failed: {path} — {e}')

    def _start_ipc_server(self):
        try:
            srv = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            srv.bind(('127.0.0.1', _IPC_PORT))
            srv.listen(5)
            threading.Thread(target=self._ipc_loop, args=(srv,), daemon=True).start()
        except Exception:
            pass

    def _ipc_loop(self, srv):
        while True:
            try:
                conn, _ = srv.accept()
                conn.close()
                self.root.after(0, self._open_settings)
            except Exception:
                break

    def _make_icon(self):
        icon_path = _resource('icon.png')
        if HAS_TRAY and os.path.exists(icon_path):
            return Image.open(icon_path).convert('RGBA').resize((256, 256))
        img = Image.new('RGB', (64, 64), '#1565C0')
        d = ImageDraw.Draw(img)
        d.rounded_rectangle([4, 4, 60, 60], radius=12, fill='#1976D2')
        for row, y in enumerate([20, 32, 44]):
            keys_in_row = 4 if row < 2 else 3
            x_start = 14 if row < 2 else 20
            for i in range(keys_in_row):
                x = x_start + i * 10
                d.rectangle([x, y, x + 7, y + 7], fill='white')
        return img

    def _start_tray(self):
        icon = self._make_icon()
        menu = pystray.Menu(
            pystray.MenuItem('설정 열기', self._tray_open, default=True),
            pystray.Menu.SEPARATOR,
            pystray.MenuItem('종료', self._tray_quit),
        )
        self._tray = pystray.Icon('ShortKey', icon, 'ShortKey', menu)
        threading.Thread(target=self._tray.run, daemon=True).start()

    def _tray_open(self, *_):
        self.root.after(0, self._open_settings)

    def _tray_quit(self, *_):
        self.root.after(0, self._quit)

    def _open_settings(self):
        if self._win is None:
            self._win = SettingsWindow(self.root, self)
        else:
            try:
                if self._win.w.winfo_exists():
                    self._win.show()
                    return
            except Exception:
                pass
            self._win = SettingsWindow(self.root, self)

    def _quit(self):
        try:
            keyboard.unhook_all_hotkeys()
        except Exception:
            pass
        if self._tray:
            try:
                self._tray.stop()
            except Exception:
                pass
        self.root.quit()

    def run(self):
        self.root.mainloop()


if __name__ == '__main__':
    if not _acquire_single_instance():
        sys.exit(0)
    App().run()
