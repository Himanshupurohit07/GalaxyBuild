package com.sparc.tc.util;

import com.sparc.tc.domain.WorkbookFileSystem;
import com.sparc.tc.exceptions.TCExceptionRuntime;
import com.sparc.tc.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class WorkBookFileSystemUtil {

    private WorkBookFileSystemUtil() {

    }

    public static void flush(final WorkbookFileSystem workbookFileSystem, final String targetDir) throws IOException {
        if (workbookFileSystem == null || workbookFileSystem.getRoot() == null || !com.sparc.tc.util.StringUtils.hasContent(targetDir)) {
            return;
        }
        final File pkg = new File(targetDir + File.separator + workbookFileSystem.getRoot().getName());
        if (!pkg.exists()) {
            final File parentFile = pkg.getParentFile();
            parentFile.mkdirs();
        }
        final FileOutputStream zipFile         = new FileOutputStream(pkg);
        final ZipOutputStream  zipOutputStream = new ZipOutputStream(zipFile);
        populateZipEntries(zipOutputStream, workbookFileSystem);
        zipOutputStream.close();
    }

    private static void populateZipEntries(final ZipOutputStream zipOutputStream, final WorkbookFileSystem wfs) throws IOException {
        final WorkbookFileSystem.Node root = wfs.getRoot();
        addLeafNodes(root, zipOutputStream, null);
    }

    private static void touchDirectory(final ZipOutputStream zipOutputStream, final WorkbookFileSystem.Node directoryNode, final String... hierarchy) throws IOException {
        if (zipOutputStream == null || directoryNode == null || !com.sparc.tc.util.StringUtils.hasContent(directoryNode.getName())) {
            return;
        }
        final String hierarchyString = getHierarchyString(hierarchy);
        final String entryName       = directoryNode.isZip() ? directoryNode.getName(hierarchyString) : directoryNode.getName(hierarchyString) + "/";
        if (directoryNode.isZip()) {
            final ZipEntry zipEntry = new ZipEntry(entryName);
            zipOutputStream.putNextEntry(zipEntry);
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final ZipOutputStream       subZipOutputStream    = new ZipOutputStream(byteArrayOutputStream);
            addLeafNodes(directoryNode, subZipOutputStream, null);
            subZipOutputStream.close();
            zipOutputStream.write(byteArrayOutputStream.toByteArray());
            zipOutputStream.closeEntry();
        } else {
            addLeafNodes(directoryNode, zipOutputStream, getHierarchy(hierarchy, directoryNode.getName()));
        }

    }

    private static void addLeafNodes(final WorkbookFileSystem.Node directoryNode, final ZipOutputStream zipOutputStream, final String... hierarchy) {
        if (directoryNode.getLeafs() != null) {
            directoryNode.getLeafs().forEach(node -> {
                try {
                    if (node.isDirectory()) {
                        touchDirectory(zipOutputStream, node, hierarchy);
                    } else {
                        touchFile(zipOutputStream, node, hierarchy);
                    }
                } catch (IOException e) {
                    throw new TCExceptionRuntime(e.getMessage(), TCExceptionRuntime.Type.ERROR);
                }
            });
        }
    }

    private static void touchFile(final ZipOutputStream zipOutputStream, final WorkbookFileSystem.Node fileNode, final String... hierarchy) throws IOException {
        if (zipOutputStream == null || fileNode == null || !StringUtils.hasContent(fileNode.getName())) {
            return;
        }
        final String   hierarchyName = getHierarchyString(hierarchy);
        final ZipEntry zipEntry      = new ZipEntry(fileNode.getName(hierarchyName));
        zipOutputStream.putNextEntry(zipEntry);
        fileNode.getData().write(zipOutputStream);
        zipOutputStream.closeEntry();
    }

    private static String[] getHierarchy(final String[] oldHierarchy, final String currentLevel) {
        if (oldHierarchy == null) {
            return new String[]{currentLevel};
        }
        final String[] newHierarchy = Arrays.copyOf(oldHierarchy, oldHierarchy.length + 1);
        newHierarchy[oldHierarchy.length] = currentLevel;
        return newHierarchy;
    }

    private static String getHierarchyString(final String... hierarchy) {
        if (hierarchy == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        for (String path : hierarchy) {
            sb.append(path + "/");
        }
        return sb.toString();
    }
}
