package com.ltst.przwrd.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 25.07.13.
 */
public class Files {

    @Nullable
    static public byte[] readFile(@Nonnull String path){
        File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return  bytes;
    }

    static public String openFileToString(String fileName) {
        String file_string;
        byte[] _bytes = readFile(fileName);

        try {
            file_string = new String(_bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // this should never happen because "UTF-8" is hard-coded.
            throw new IllegalStateException(e);
        }
        return file_string;
    }
}
