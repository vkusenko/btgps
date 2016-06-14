package com.vkusenko.btgpslogger.events;

import java.io.File;
import java.util.List;

public class ListFileEvent {
    public final List<File> listFiles;

    public ListFileEvent(List<File> listFile) {
        this.listFiles = listFile;
    }
}
