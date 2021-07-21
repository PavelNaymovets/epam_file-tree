package com.efimchick.ifmo.io.filetree;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileTreeImpl implements FileTree {

    private final StringBuilder sb = new StringBuilder();

    @Override
    public Optional<String> tree(Path path) {

        File file = new File(String.valueOf(path));
        if (!file.exists()) {
            return Optional.empty();
        }
        if (file.isFile()) {
            return Optional.of(file.getName() + " " + file.length() + " bytes");
        }
        int level = 0;
        List<Boolean> hierarchyTree = new ArrayList<>();
        renderFolder(file, level, false, new ArrayList<>(), true);
        File[] subFiles = file.listFiles(File :: isFile);
        assert subFiles != null;
        Arrays.sort(subFiles, Comparator.comparing(a -> a.getName().toLowerCase(Locale.ROOT)));
        for (int j = 0; j < subFiles.length; j++) {
            boolean lastFile = ((j + 1) == subFiles.length);
            hierarchyTree.add(true);
            indent(level + 2, lastFile, hierarchyTree);
            sb.append(subFiles[j].getName()).append(" ").append(subFiles[j].length()).append(" bytes\n");
            hierarchyTree.remove(hierarchyTree.size() - 1);
        }

        return Optional.of(sb.toString());
    }

    private void renderFolder(File file, int level, boolean isLast, List<Boolean> hierarchyTree, boolean subLast) {
        indent(level, isLast, hierarchyTree);
        sb.append(file.getName()).append(" ").append(calcFolderSize(file.toPath())).append(" bytes\n");
        File[] subDirs = file.listFiles(File::isDirectory);
        assert subDirs != null;
        Arrays.sort(subDirs, Comparator.comparing(a -> a.getName().toLowerCase(Locale.ROOT)));
        subLast = file.listFiles(File::isFile).length == 0;

        for (int i = 0; i < subDirs.length; i++) {
            File[] subFiles = subDirs[i].listFiles(File::isFile);
            Arrays.sort(subFiles, Comparator.comparing(a -> a.getName().toLowerCase(Locale.ROOT)));
            boolean last = ((i + 1) == subDirs.length && subLast);
            hierarchyTree.add(!last);
            renderFolder(subDirs[i], level + 1, last, hierarchyTree, subLast);
            for (int j = 0; j < subFiles.length; j++) {
                boolean lastFile = ((j + 1) == subFiles.length);
                hierarchyTree.add(subLast);
                indent(level + 2, lastFile, hierarchyTree);
                sb.append(subFiles[j].getName()).append(" ").append(subFiles[j].length()).append(" bytes\n");
                hierarchyTree.remove(hierarchyTree.size() - 1);
            }
            hierarchyTree.remove(hierarchyTree.size() - 1);
        }
    }

    private void indent(int level, boolean isLast, List<Boolean> hierarchyTree) {
        String indentStr = "│  ";

        for (int i = 0; i < hierarchyTree.size() - 1; ++i) {
            if (hierarchyTree.get(i)) {
                sb.append(indentStr);
            } else {
                sb.append("   ");
            }
        }

        if (level > 0) {
            sb.append(isLast ? "└─ " : "├─ ");
        }
    }

    private static long calcFolderSize(Path path) {
        long size = 0;
        try {
            size = Files.walk(path)
                    .filter(f -> f.toFile().isFile())
                    .mapToLong(f -> f.toFile().length())
                    .sum();
        } catch (NullPointerException | IllegalArgumentException | IOException e) {
            e.printStackTrace();
        }
        return size;
    }
}

