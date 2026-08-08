@echo off
chcp 65001 > nul
echo ========================================
echo  ShortKey 빌드 스크립트
echo ========================================
echo.

echo [1/2] 필요한 패키지 설치 중...
pip install pyinstaller keyboard pystray Pillow
if errorlevel 1 (
    echo 패키지 설치 실패!
    pause
    exit /b 1
)

echo.
echo [2/2] 실행 파일(.exe) 생성 중...
pyinstaller ^
    --onefile ^
    --windowed ^
    --name ShortKey ^
    --hidden-import pystray._win32 ^
    main.py

echo.
if exist dist\ShortKey.exe (
    echo ========================================
    echo  빌드 완료!
    echo  dist\ShortKey.exe 파일을 실행하세요.
    echo ========================================
) else (
    echo 빌드 실패. 위의 오류를 확인하세요.
)
pause
