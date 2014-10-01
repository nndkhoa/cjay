package com.cloudjay.cjay.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.io.Serializable;

public class IsoCode implements Serializable {

    public int id;
    public int type;
    public String code;
    public String full_name;

	@Override
	public String toString() {
		return code +": " +full_name;
	}

	public IsoCode(int id, int type, String code, String full_name) {
        id = this.id;
        type = this.type;
        code = this.code;
		full_name = this.full_name;
    }

    public static final String TABLE = "iso_code";
	public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String CODE = "code";
    public static final String DISPLAY_NAME = "display_name";

    public static final Uri URI = Uri.parse("content://" + User.AUTHORITY + "/" + TABLE);

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(ID, id);
        values.put(TYPE, type);
        values.put(CODE, code);
        values.put(DISPLAY_NAME, full_name);

        return values;
    }

    /* Create IsoCode Object from a cursor */
    public static IsoCode isoCodeObject(Cursor isoCodeCursor) {
        int id = isoCodeCursor.getInt(isoCodeCursor.getColumnIndex(ID));
        int type = isoCodeCursor.getInt(isoCodeCursor.getColumnIndex(TYPE));
        String code = isoCodeCursor.getString(isoCodeCursor.getColumnIndex(CODE));
        String displayName = isoCodeCursor.getString(isoCodeCursor.getColumnIndex(DISPLAY_NAME));

        return new IsoCode(id, type, code, displayName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IsoCode isoCode = (IsoCode) o;

        if (id != isoCode.id) return false;
        if (type != isoCode.type) return false;
        if (!full_name.equals(isoCode.full_name)) return false;
        if (!code.equals(isoCode.code)) return false;

        return true;
    }
}
