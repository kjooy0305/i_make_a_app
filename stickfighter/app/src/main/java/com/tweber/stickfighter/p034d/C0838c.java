package com.tweber.stickfighter.p034d;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.PointF;
import android.util.Log;
import android.util.SparseArray;
import com.tweber.stickfighter.activities.HomeScreenActivity;
import com.tweber.stickfighter.p038h.AbstractC0908f;
import com.tweber.stickfighter.p038h.C0904b;
import com.tweber.stickfighter.p038h.C0905c;
import com.tweber.stickfighter.p038h.C0907e;
import com.tweber.stickfighter.p038h.C0909g;
import com.tweber.stickfighter.p038h.C0918p;
import com.tweber.stickfighter.p038h.C0924v;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/* JADX INFO: renamed from: com.tweber.stickfighter.d.c */
/* JADX INFO: loaded from: classes.dex */
public class C0838c extends SQLiteOpenHelper implements InterfaceC0837b {

    /* JADX INFO: renamed from: a */
    static final String f3887a = "/data/data/" + HomeScreenActivity.class.getPackage().getName() + "/";

    /* JADX INFO: renamed from: b */
    SQLiteDatabase f3888b;

    protected C0838c(Context context) {
        super(context, f3887a + "StickFighter", (SQLiteDatabase.CursorFactory) null, 4);
        this.f3888b = getWritableDatabase();
    }

    /* JADX INFO: renamed from: a */
    private C0924v m4575a(Cursor cursor) {
        return new C0924v(cursor.getInt(cursor.getColumnIndex("_id")), cursor.getString(cursor.getColumnIndex("name")), cursor.getFloat(cursor.getColumnIndex("height_width_ratio")), cursor.getInt(cursor.getColumnIndex("background_color")));
    }

    /* JADX INFO: renamed from: a */
    private HashMap m4576a(Set set, long j) {
        if (set == null || set.isEmpty()) {
            return new HashMap();
        }
        StringBuilder sb = new StringBuilder((set.size() * 2) - 1);
        Iterator it = set.iterator();
        while (it.hasNext()) {
            Integer num = (Integer) it.next();
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(num);
        }
        Cursor cursorRawQuery = this.f3888b.rawQuery("SELECT a._id _id, a.animation_object_type_id animation_object_type_id, c.argb_color argb_color FROM animation_object a LEFT JOIN animation_object_detail c ON a._id = c.animation_object_id AND c.frame_id = " + j + " WHERE a._id IN (" + sb.toString() + ")", null);
        int columnIndex = cursorRawQuery.getColumnIndex("_id");
        int columnIndex2 = cursorRawQuery.getColumnIndex("animation_object_type_id");
        int columnIndex3 = cursorRawQuery.getColumnIndex("argb_color");
        HashMap map = new HashMap();
        while (cursorRawQuery.moveToNext()) {
            int i = cursorRawQuery.getInt(columnIndex);
            int i2 = cursorRawQuery.getInt(columnIndex3);
            if (i2 == 0) {
                i2 = -16777216;
            }
            AbstractC0908f abstractC0908fM4885a = C0909g.m4885a(cursorRawQuery.getInt(columnIndex2), i, j, new ArrayList(), i2);
            map.put(Integer.valueOf(abstractC0908fM4885a.f4181b), abstractC0908fM4885a);
        }
        cursorRawQuery.close();
        return map;
    }

    /* JADX INFO: renamed from: a */
    private void m4577a(SQLiteDatabase sQLiteDatabase, String str) {
        try {
            sQLiteDatabase.execSQL(str);
        } catch (Exception e) {
        }
    }

    /* JADX INFO: renamed from: a */
    private void m4578a(C0905c c0905c, long j, long j2) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("x", Float.valueOf(c0905c.m4863c()));
        contentValues.put("y", Float.valueOf(c0905c.m4865d()));
        this.f3888b.update("anchor_point", contentValues, "frame_id = ? AND index_number = ? AND animation_object_id = ?", new String[]{String.valueOf(j), String.valueOf(c0905c.f4166c), String.valueOf(j2)});
    }

    /* JADX INFO: renamed from: a */
    private void m4579a(AbstractC0908f abstractC0908f, long j) {
        mo4555a(abstractC0908f.f4181b, j, abstractC0908f.m4880b());
        Iterator it = abstractC0908f.m4872a().iterator();
        while (it.hasNext()) {
            m4578a((C0905c) it.next(), j, abstractC0908f.f4181b);
        }
    }

    /* JADX INFO: renamed from: b */
    private C0918p m4580b(Cursor cursor) {
        return new C0918p(cursor.getInt(cursor.getColumnIndex("_id")), cursor.getInt(cursor.getColumnIndex("position")), cursor.getInt(cursor.getColumnIndex("sequence_id")));
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: a */
    public C0905c mo4548a(int i, long j, PointF pointF, int i2, int i3) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("animation_object_id", Integer.valueOf(i));
        contentValues.put("frame_id", Long.valueOf(j));
        contentValues.put("index_number", Integer.valueOf(i2));
        contentValues.put("x", Float.valueOf(pointF.x));
        contentValues.put("y", Float.valueOf(pointF.y));
        contentValues.put("selectable", Integer.valueOf(i3 & 1));
        contentValues.put("anchor_point_type", Integer.valueOf((i3 & 4) | (i3 & 2) | (i3 & 8)));
        return new C0905c(this.f3888b.insert("anchor_point", "x", contentValues), i2, pointF.x, pointF.y, i3);
    }

    /* JADX INFO: renamed from: a */
    public AbstractC0908f m4581a(AbstractC0908f abstractC0908f, long j, HashMap map, SparseArray sparseArray) {
        ArrayList arrayListM4872a = abstractC0908f.m4872a();
        ArrayList arrayList = new ArrayList();
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= arrayListM4872a.size()) {
                return C0909g.m4885a(abstractC0908f.f4182c, abstractC0908f.f4181b, j, arrayList, abstractC0908f.m4880b());
            }
            C0905c c0905c = (C0905c) arrayListM4872a.get(i2);
            C0905c c0905cMo4548a = mo4548a(abstractC0908f.f4181b, j, new PointF(c0905c.m4863c(), c0905c.m4865d()), c0905c.f4166c, c0905c.f4165b);
            map.put(c0905cMo4548a, c0905c);
            sparseArray.put(c0905c.f4164a, Integer.valueOf(c0905cMo4548a.f4164a));
            arrayList.add(c0905cMo4548a);
            i = i2 + 1;
        }
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: a */
    public AbstractC0908f mo4549a(C0918p c0918p, int i, int i2, C0907e... c0907eArr) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("sequence_id", Integer.valueOf(c0918p.f4233b));
        contentValues.put("animation_object_type_id", Integer.valueOf(i));
        int iInsert = (int) this.f3888b.insert("animation_object", "animation_object_type_id", contentValues);
        ArrayList arrayList = new ArrayList();
        for (int i3 = 0; i3 < c0907eArr.length; i3++) {
            arrayList.add(mo4548a(iInsert, c0918p.f4232a, c0907eArr[i3].f4178a, i3, c0907eArr[i3].f4179b));
        }
        AbstractC0908f abstractC0908fM4885a = C0909g.m4885a(i, iInsert, c0918p.f4232a, arrayList, i2);
        c0918p.m4924a(abstractC0908fM4885a);
        return abstractC0908fM4885a;
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: a */
    public C0918p mo4550a(int i, int i2) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("sequence_id", Integer.valueOf(i));
        contentValues.put("position", Integer.valueOf(i2));
        return new C0918p((int) this.f3888b.insert("frame", "position", contentValues), i2, i);
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: a */
    public C0924v mo4551a(long j) {
        Cursor cursorRawQuery = this.f3888b.rawQuery("SELECT _id, name, height_width_ratio, background_image_path, background_color FROM sequence WHERE _id = ?", new String[]{String.valueOf(j)});
        C0924v c0924vM4575a = cursorRawQuery.moveToFirst() ? m4575a(cursorRawQuery) : null;
        cursorRawQuery.close();
        return c0924vM4575a;
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: a */
    public C0924v mo4552a(String str, float f) {
        return mo4553a(str, f, -1);
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: a */
    public C0924v mo4553a(String str, float f, int i) {
        long jM4582d = m4582d(1, 9999) + 1;
        ContentValues contentValues = new ContentValues();
        contentValues.put("_id", Long.valueOf(jM4582d));
        contentValues.put("name", str);
        contentValues.put("height_width_ratio", Float.valueOf(f));
        contentValues.put("background_color", Integer.valueOf(i));
        return new C0924v((int) this.f3888b.insert("sequence", "name", contentValues), str, f, i);
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: a */
    public ArrayList mo4554a() {
        Cursor cursorRawQuery = this.f3888b.rawQuery("SELECT _id, name, height_width_ratio, background_image_path, background_color FROM sequence WHERE _id < 10000 ORDER BY _id DESC", null);
        ArrayList arrayList = new ArrayList();
        while (cursorRawQuery.moveToNext()) {
            arrayList.add(m4575a(cursorRawQuery));
        }
        cursorRawQuery.close();
        return arrayList;
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: a */
    public void mo4555a(int i, long j, int i2) {
        String[] strArr = {String.valueOf(i), String.valueOf(j)};
        ContentValues contentValues = new ContentValues();
        contentValues.put("argb_color", Integer.valueOf(i2));
        if (this.f3888b.update("animation_object_detail", contentValues, "animation_object_id = ? AND frame_id = ?", strArr) == 0) {
            contentValues.put("animation_object_id", Integer.valueOf(i));
            contentValues.put("frame_id", Long.valueOf(j));
            this.f3888b.insert("animation_object_detail", "frame_id", contentValues);
        }
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: a */
    public void mo4556a(long j, int i) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("background_color", Integer.valueOf(i));
        this.f3888b.update("sequence", contentValues, "_id = ?", new String[]{String.valueOf(j)});
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: a */
    public void mo4557a(C0905c c0905c, C0905c c0905c2) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("anchor_point_id", Integer.valueOf(c0905c.f4164a));
        contentValues.put("connected_anchor_point_id", Integer.valueOf(c0905c2.f4164a));
        this.f3888b.insert("anchor_connection", "connected_anchor_point_id", contentValues);
        c0905c.m4855a(new C0904b(c0905c2.f4164a));
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put("anchor_point_id", Integer.valueOf(c0905c2.f4164a));
        contentValues2.put("connected_anchor_point_id", Integer.valueOf(c0905c.f4164a));
        this.f3888b.insert("anchor_connection", "connected_anchor_point_id", contentValues2);
        c0905c2.m4855a(new C0904b(c0905c.f4164a));
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: a */
    public void mo4558a(AbstractC0908f abstractC0908f, AbstractC0908f abstractC0908f2) {
        ArrayList<C0905c> arrayListM4872a = abstractC0908f.m4872a();
        ArrayList<C0905c> arrayListM4872a2 = abstractC0908f2.m4872a();
        for (C0905c c0905c : arrayListM4872a) {
            for (C0905c c0905c2 : arrayListM4872a2) {
                if (c0905c.m4863c() == c0905c2.m4863c() && c0905c.m4865d() == c0905c2.m4865d()) {
                    mo4557a(c0905c, c0905c2);
                    return;
                }
            }
        }
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: a */
    public void mo4559a(C0918p c0918p) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("sequence_id", Integer.valueOf(c0918p.f4233b));
        contentValues.put("position", Integer.valueOf(c0918p.m4927b()));
        try {
            this.f3888b.beginTransaction();
            this.f3888b.update("frame", contentValues, "_id = ?", new String[]{String.valueOf(c0918p.f4232a)});
            Iterator it = c0918p.m4922a().iterator();
            while (it.hasNext()) {
                m4579a((AbstractC0908f) it.next(), c0918p.f4232a);
            }
            this.f3888b.setTransactionSuccessful();
        } finally {
            this.f3888b.endTransaction();
        }
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: a */
    public void mo4560a(C0918p c0918p, AbstractC0908f abstractC0908f) {
        for (C0905c c0905c : (Iterable<C0905c>) abstractC0908f.m4872a()) {
            this.f3888b.delete("anchor_point", "_id = ?", new String[]{String.valueOf(c0905c.f4164a)});
            this.f3888b.delete("anchor_connection", "anchor_point_id = ? OR connected_anchor_point_id = ?", new String[]{String.valueOf(c0905c.f4164a)});
        }
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: a */
    public void mo4561a(C0924v c0924v) {
        this.f3888b.delete("sequence", "_id = ?", new String[]{String.valueOf(c0924v.m4935b())});
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: b */
    public int mo4562b(long j) {
        Cursor cursorRawQuery = this.f3888b.rawQuery("SELECT count(*) framecount FROM frame WHERE sequence_id = ?", new String[]{String.valueOf(j)});
        int i = cursorRawQuery.moveToFirst() ? cursorRawQuery.getInt(cursorRawQuery.getColumnIndex("framecount")) : 0;
        cursorRawQuery.close();
        return i;
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: b */
    public C0904b mo4563b(int i, int i2) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("anchor_point_id", Integer.valueOf(i));
        contentValues.put("connected_anchor_point_id", Integer.valueOf(i2));
        this.f3888b.insert("anchor_connection", "connected_anchor_point_id", contentValues);
        return new C0904b(i2);
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: b */
    public C0918p mo4564b(long j, int i) {
        Cursor cursorRawQuery = this.f3888b.rawQuery("SELECT _id, position, sequence_id FROM frame WHERE sequence_id = ? AND position = ?", new String[]{String.valueOf(j), String.valueOf(i)});
        C0918p c0918pM4580b = cursorRawQuery.moveToNext() ? m4580b(cursorRawQuery) : null;
        cursorRawQuery.close();
        if (c0918pM4580b == null) {
            return null;
        }
        HashSet hashSet = new HashSet();
        Cursor cursorRawQuery2 = this.f3888b.rawQuery("SELECT a.* FROM anchor_point a WHERE a.frame_id = ?", new String[]{String.valueOf(c0918pM4580b.f4232a)});
        int columnIndex = cursorRawQuery2.getColumnIndex("_id");
        int columnIndex2 = cursorRawQuery2.getColumnIndex("index_number");
        int columnIndex3 = cursorRawQuery2.getColumnIndex("x");
        int columnIndex4 = cursorRawQuery2.getColumnIndex("y");
        int columnIndex5 = cursorRawQuery2.getColumnIndex("selectable");
        int columnIndex6 = cursorRawQuery2.getColumnIndex("anchor_point_type");
        int columnIndex7 = cursorRawQuery2.getColumnIndex("animation_object_id");
        HashMap map = new HashMap();
        HashMap map2 = new HashMap();
        while (cursorRawQuery2.moveToNext()) {
            int i2 = cursorRawQuery2.getInt(columnIndex);
            int i3 = cursorRawQuery2.getInt(columnIndex7);
            C0905c c0905c = new C0905c(i2, cursorRawQuery2.getInt(columnIndex2), cursorRawQuery2.getFloat(columnIndex3), cursorRawQuery2.getFloat(columnIndex4), cursorRawQuery2.getInt(columnIndex5) | cursorRawQuery2.getInt(columnIndex6));
            map2.put(Integer.valueOf(i2), Integer.valueOf(i3));
            hashSet.add(Integer.valueOf(i3));
            map.put(Integer.valueOf(c0905c.f4164a), c0905c);
        }
        cursorRawQuery2.close();
        HashMap mapM4576a = m4576a(hashSet, c0918pM4580b.f4232a);
        for (Map.Entry entry : (Iterable<Map.Entry>) map2.entrySet()) {
            AbstractC0908f abstractC0908f = (AbstractC0908f) mapM4576a.get(entry.getValue());
            mapM4576a.put(entry.getValue(), abstractC0908f);
            abstractC0908f.m4876a((C0905c) map.get(entry.getKey()));
        }
        Iterator it = mapM4576a.values().iterator();
        while (it.hasNext()) {
            Collections.sort(((AbstractC0908f) it.next()).m4872a());
        }
        for (AbstractC0908f abstractC0908f2 : (Iterable<AbstractC0908f>) mapM4576a.values()) {
            if (!abstractC0908f2.m4872a().isEmpty()) {
                c0918pM4580b.m4924a(abstractC0908f2);
            }
        }
        Cursor cursorRawQuery3 = this.f3888b.rawQuery("SELECT a.* FROM anchor_connection a INNER JOIN anchor_point b ON a.anchor_point_id = b._id WHERE b.frame_id = ?", new String[]{String.valueOf(c0918pM4580b.f4232a)});
        int columnIndex8 = cursorRawQuery3.getColumnIndex("connected_anchor_point_id");
        int columnIndex9 = cursorRawQuery3.getColumnIndex("anchor_point_id");
        while (cursorRawQuery3.moveToNext()) {
            ((C0905c) map.get(Integer.valueOf(cursorRawQuery3.getInt(columnIndex9)))).m4855a(new C0904b(cursorRawQuery3.getInt(columnIndex8)));
        }
        cursorRawQuery3.close();
        return c0918pM4580b;
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: b */
    public C0918p mo4565b(C0918p c0918p) {
        C0918p c0918pMo4550a = mo4550a(c0918p.f4233b, c0918p.m4927b() + 1);
        ArrayList arrayListM4922a = c0918p.m4922a();
        HashMap map = new HashMap();
        SparseArray sparseArray = new SparseArray();
        ArrayList arrayList = new ArrayList();
        try {
            this.f3888b.beginTransaction();
            for (int i = 0; i < arrayListM4922a.size(); i++) {
                AbstractC0908f abstractC0908fM4581a = m4581a((AbstractC0908f) arrayListM4922a.get(i), c0918pMo4550a.f4232a, map, sparseArray);
                arrayList.add(abstractC0908fM4581a);
                c0918pMo4550a.m4924a(abstractC0908fM4581a);
            }
            for (int i2 = 0; i2 < arrayList.size(); i2++) {
                ArrayList arrayListM4872a = ((AbstractC0908f) arrayList.get(i2)).m4872a();
                for (int i3 = 0; i3 < arrayListM4872a.size(); i3++) {
                    C0905c c0905c = (C0905c) arrayListM4872a.get(i3);
                    ArrayList arrayListM4866e = ((C0905c) map.get(c0905c)).m4866e();
                    for (int i4 = 0; i4 < arrayListM4866e.size(); i4++) {
                        c0905c.m4855a(mo4563b(c0905c.f4164a, ((Integer) sparseArray.get(((C0904b) arrayListM4866e.get(i4)).f4162a)).intValue()));
                    }
                }
            }
            this.f3888b.setTransactionSuccessful();
            return c0918pMo4550a;
        } finally {
            this.f3888b.endTransaction();
        }
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: b */
    public C0924v mo4566b(String str, float f) {
        int iM4582d = m4582d(10000, Integer.MAX_VALUE) + 1;
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", str);
        contentValues.put("_id", Integer.valueOf(iM4582d));
        contentValues.put("height_width_ratio", Float.valueOf(f));
        contentValues.put("background_color", (Integer) (-1));
        this.f3888b.insert("sequence", "name", contentValues);
        return new C0924v(iM4582d, str, f, -1);
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: b */
    public ArrayList mo4567b() {
        Cursor cursorRawQuery = this.f3888b.rawQuery("SELECT _id, name, height_width_ratio, background_image_path, background_color FROM sequence WHERE _id > 9999 ORDER BY _id DESC", null);
        ArrayList arrayList = new ArrayList();
        while (cursorRawQuery.moveToNext()) {
            arrayList.add(m4575a(cursorRawQuery));
        }
        cursorRawQuery.close();
        return arrayList;
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: b */
    public void mo4568b(C0924v c0924v) {
        String[] strArr = {String.valueOf(c0924v.m4935b())};
        this.f3888b.execSQL("DELETE FROM animation_object_detail WHERE animation_object_id IN (SELECT _id FROM animation_object WHERE sequence_id = ?)", strArr);
        this.f3888b.execSQL("DELETE FROM animation_object WHERE sequence_id = ?", strArr);
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: c */
    public void mo4569c() {
        this.f3888b.beginTransaction();
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: c */
    public void mo4570c(int i, int i2) {
        String[] strArr = {String.valueOf(i)};
        ContentValues contentValues = new ContentValues();
        contentValues.put("selectable", Integer.valueOf(i2 & 1));
        contentValues.put("anchor_point_type", Integer.valueOf((i2 & 4) | (i2 & 2) | (i2 & 8)));
        this.f3888b.update("anchor_point", contentValues, "_id = ?", strArr);
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: c */
    public void mo4571c(long j, int i) {
        this.f3888b.execSQL(String.format("UPDATE frame SET position = position + 1 WHERE sequence_id = %s AND position >= %s", Long.valueOf(j), Integer.valueOf(i)));
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: c */
    public void mo4572c(C0918p c0918p) {
        this.f3888b.delete("anchor_connection", "anchor_point_id IN (SELECT _id FROM anchor_point WHERE frame_id = ?)", new String[]{String.valueOf(c0918p.f4232a)});
        this.f3888b.delete("anchor_point", "frame_id = ?", new String[]{String.valueOf(c0918p.f4232a)});
        this.f3888b.delete("frame", "_id = ?", new String[]{String.valueOf(c0918p.f4232a)});
        this.f3888b.execSQL("UPDATE frame SET position = position - 1 WHERE position > " + c0918p.m4927b() + " AND sequence_id = " + c0918p.f4233b);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper, java.lang.AutoCloseable
    public synchronized void close() {
        if (this.f3888b != null && this.f3888b.isOpen()) {
            this.f3888b.close();
            super.close();
        }
    }

    /* JADX INFO: renamed from: d */
    public int m4582d(int i, int i2) {
        Cursor cursorRawQuery = this.f3888b.rawQuery("SELECT max(_id) as MaxId FROM sequence WHERE _id >= ? AND _id <= ?", new String[]{String.valueOf(i), String.valueOf(i2)});
        int i3 = cursorRawQuery.moveToFirst() ? cursorRawQuery.getInt(0) : 0;
        cursorRawQuery.close();
        return Math.max(i3, i);
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: d */
    public void mo4573d() {
        this.f3888b.setTransactionSuccessful();
    }

    @Override // com.tweber.stickfighter.p034d.InterfaceC0837b
    /* JADX INFO: renamed from: e */
    public void mo4574e() {
        this.f3888b.endTransaction();
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS 'sequence' (\n\t'height_width_ratio'\tNUMERIC,\n\t'name'\tTEXT,\n\t'_id'\tINTEGER,\n\tPRIMARY KEY(_id)\n);");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS 'frame' (\n\t'position'\tNUMERIC,\n\t'sequence_id'\tNUMERIC,\n\t'_id'\tINTEGER,\n\tPRIMARY KEY(_id)\n);");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS 'animation_object_type' (\n\t'name'\tTEXT,\n\t'_id'\tINTEGER,\n\tPRIMARY KEY(_id)\n);");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS 'animation_object' (\n\t'sequence_id'\tNUMERIC,\n\t'animation_object_type_id'\tNUMERIC,\n\t'_id'\tINTEGER,\n\tPRIMARY KEY(_id)\n);");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS 'anchor_point' (\n\t'anchor_point_type'\tNUMERIC,\n\t'selectable'\tNUMERIC,\n\t'index_number'\tNUMERIC,\n\t'frame_id'\tNUMERIC,\n\t'animation_object_id'\tNUMERIC,\n\t'_id'\tINTEGER,\n\t'x'\tNUMERIC,\n\t'y'\tNUMERIC,\n\tPRIMARY KEY(_id)\n);");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS 'anchor_connection' (\n\t'_id'\tINTEGER,\n\t'anchor_point_id'\tNUMERIC,\n\t'connected_anchor_point_id'\tNUMERIC,\n\tPRIMARY KEY(_id)\n);");
        m4577a(sQLiteDatabase, "ALTER TABLE sequence ADD COLUMN background_image_path TEXT");
        m4577a(sQLiteDatabase, "ALTER TABLE sequence ADD COLUMN background_color INTEGER");
        m4577a(sQLiteDatabase, "CREATE TABLE IF NOT EXISTS animation_object_detail (_id integer primary key autoincrement, animation_object_id integer, frame_id integer, argb_color integer)");
        m4577a(sQLiteDatabase, "CREATE TABLE IF NOT EXISTS figure_definition (_id integer primary key autoincrement, name text)");
        m4577a(sQLiteDatabase, "CREATE TABLE IF NOT EXISTS figure_definition_part (_id integer primary key autoincrement, animation_object_id integer, figure_definition_id integer)");
        m4577a(sQLiteDatabase, "CREATE INDEX IF NOT EXISTS anchor_connection_anchor_point_id ON anchor_connection(anchor_point_id)");
        m4577a(sQLiteDatabase, "CREATE INDEX IF NOT EXISTS anchor_point_animation_object_id ON anchor_point(frame_id)");
        m4577a(sQLiteDatabase, "CREATE INDEX IF NOT EXISTS animation_object_sequence_id ON animation_object(sequence_id)");
        m4577a(sQLiteDatabase, "CREATE INDEX IF NOT EXISTS animation_object_detail_id ON animation_object_detail(animation_object_id)");
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        if (i < 2) {
            sQLiteDatabase.execSQL("ALTER TABLE sequence ADD COLUMN background_image_path TEXT");
            sQLiteDatabase.execSQL("ALTER TABLE sequence ADD COLUMN background_color INTEGER");
            sQLiteDatabase.execSQL("CREATE TABLE animation_object_detail (_id integer primary key autoincrement, animation_object_id integer, frame_id integer, argb_color integer)");
            sQLiteDatabase.execSQL("CREATE TABLE figure_definition (_id integer primary key autoincrement, name text)");
            sQLiteDatabase.execSQL("CREATE TABLE figure_definition_part (_id integer primary key autoincrement, animation_object_id integer, figure_definition_id integer)");
            return;
        }
        if (i < 3) {
            try {
                sQLiteDatabase.execSQL("CREATE INDEX anchor_connection_anchor_point_id ON anchor_connection(anchor_point_id)");
                sQLiteDatabase.execSQL("CREATE INDEX anchor_point_animation_object_id ON anchor_point(frame_id)");
                sQLiteDatabase.execSQL("CREATE INDEX animation_object_sequence_id ON animation_object(sequence_id)");
                sQLiteDatabase.execSQL("CREATE INDEX animation_object_detail_id ON animation_object_detail(animation_object_id)");
            } catch (Exception e) {
                Log.e("DATABASE", e.getMessage());
            }
        }
    }
}
