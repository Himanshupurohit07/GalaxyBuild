package com.sparc.tc.domain;

import com.sparc.tc.util.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class WorkbookFileSystem {

    public enum NodeType {
        DIRECTORY, FILE;
    }


    public static class Node {
        private NodeType type;
        private String name;
        private XSSFWorkbook data;
        private boolean zip;
        private List<Node> leafs;

        public String getName(final String hierarchyName) {
            if (hierarchyName == null || hierarchyName.isEmpty()) {
                return getName();
            }
            if (hierarchyName.endsWith("/")) {
                return hierarchyName + getName();
            }
            return hierarchyName + "/" + getName();
        }

        public void add(final Node node) {
            if (this.leafs == null) {
                this.leafs = new LinkedList<>();
            }
            this.leafs.add(node);
        }

        public Node getNode(final String name) {
            if (!StringUtils.hasContent(name)) {
                return this;
            }
            if (this.name.equals(name)) {
                return this;
            }
            if (leafs == null) {
                return null;
            }
            return leafs.stream().filter(node -> node.getName().equals(name)).findAny().orElse(null);
        }

        public boolean isDirectory() {
            return type == NodeType.DIRECTORY;
        }


        //<editor-fold defaultstate="collapsed" desc="delombok">
        @SuppressWarnings("all")
        public static class NodeBuilder {
            @SuppressWarnings("all")
            private NodeType type;
            @SuppressWarnings("all")
            private String name;
            @SuppressWarnings("all")
            private XSSFWorkbook data;
            @SuppressWarnings("all")
            private boolean zip;
            @SuppressWarnings("all")
            private List<Node> leafs;

            @SuppressWarnings("all")
            NodeBuilder() {
            }

            @SuppressWarnings("all")
            public WorkbookFileSystem.Node.NodeBuilder type(final NodeType type) {
                this.type = type;
                return this;
            }

            @SuppressWarnings("all")
            public WorkbookFileSystem.Node.NodeBuilder name(final String name) {
                this.name = name;
                return this;
            }

            @SuppressWarnings("all")
            public WorkbookFileSystem.Node.NodeBuilder data(final XSSFWorkbook data) {
                this.data = data;
                return this;
            }

            @SuppressWarnings("all")
            public WorkbookFileSystem.Node.NodeBuilder zip(final boolean zip) {
                this.zip = zip;
                return this;
            }

            @SuppressWarnings("all")
            public WorkbookFileSystem.Node.NodeBuilder leafs(final List<Node> leafs) {
                this.leafs = leafs;
                return this;
            }

            @SuppressWarnings("all")
            public WorkbookFileSystem.Node build() {
                return new Node(this.type, this.name, this.data, this.zip, this.leafs);
            }

            @Override
            @SuppressWarnings("all")
            public String toString() {
                return "WorkbookFileSystem.Node.NodeBuilder(type=" + this.type + ", name=" + this.name + ", data=" + this.data + ", zip=" + this.zip + ", leafs=" + this.leafs + ")";
            }
        }

        @SuppressWarnings("all")
        public static WorkbookFileSystem.Node.NodeBuilder builder() {
            return new NodeBuilder();
        }

        @SuppressWarnings("all")
        public NodeType getType() {
            return this.type;
        }

        @SuppressWarnings("all")
        public String getName() {
            return this.name;
        }

        @SuppressWarnings("all")
        public XSSFWorkbook getData() {
            return this.data;
        }

        @SuppressWarnings("all")
        public boolean isZip() {
            return this.zip;
        }

        @SuppressWarnings("all")
        public List<Node> getLeafs() {
            return this.leafs;
        }

        @SuppressWarnings("all")
        public void setType(final NodeType type) {
            this.type = type;
        }

        @SuppressWarnings("all")
        public void setName(final String name) {
            this.name = name;
        }

        @SuppressWarnings("all")
        public void setData(final XSSFWorkbook data) {
            this.data = data;
        }

        @SuppressWarnings("all")
        public void setZip(final boolean zip) {
            this.zip = zip;
        }

        @SuppressWarnings("all")
        public void setLeafs(final List<Node> leafs) {
            this.leafs = leafs;
        }

        @SuppressWarnings("all")
        public Node() {
        }

        @SuppressWarnings("all")
        public Node(final NodeType type, final String name, final XSSFWorkbook data, final boolean zip, final List<Node> leafs) {
            this.type = type;
            this.name = name;
            this.data = data;
            this.zip = zip;
            this.leafs = leafs;
        }
        //</editor-fold>
    }

    private Node root;

    public void touchFile(final String fileName, final XSSFWorkbook data, final String... treePath) {
        if (!StringUtils.hasContent(fileName)) {
            return;
        }
        if (this.root == null) {
            this.root = new Node();
        }
        if (treePath == null) {
            this.root.setType(NodeType.FILE);
            this.root.setName(fileName);
            this.root.setData(data);
        } else {
            final Node node = getNode(this.root, treePath);
            if (node != null) {
                final boolean fileExists = node.getLeafs() == null ? false : node.getLeafs().stream().anyMatch(leaf -> leaf.getName().equals(fileName));
                if (!fileExists) {
                    node.add(Node.builder().name(fileName).data(data).type(NodeType.FILE).build());
                }
            } else {
                touchDirectory(treePath[treePath.length - 1], false, Arrays.copyOfRange(treePath, 0, treePath.length - 2));
                final Node updatedNode = getNode(this.root, treePath);
                updatedNode.add(Node.builder().name(fileName).data(data).type(NodeType.FILE).build());
            }
        }
    }

    public void touchDirectory(final String dirName, final boolean zip, final String... treePath) {
        if (!StringUtils.hasContent(dirName)) {
            return;
        }
        if (this.root == null) {
            this.root = new Node();
        }
        if (treePath == null) {
            this.root.setType(NodeType.DIRECTORY);
            this.root.setName(dirName);
            this.root.setZip(zip);
            this.root.setData(null);
        } else {
            final Node node = getNode(this.root, treePath);
            if (node != null) {
                final boolean directoryExists = node.getLeafs() == null ? false : node.getLeafs().stream().anyMatch(leaf -> leaf.getName().equals(dirName));
                if (!directoryExists) {
                    node.add(Node.builder().name(dirName).type(NodeType.DIRECTORY).zip(zip).build());
                }
            } else {
                touchDirectory(this.root, dirName, zip, treePath);
            }
        }
    }

    public void touchDirectory(final Node node, final String dirName, final boolean zip, final String... treePath) {
        if (node == null || !StringUtils.hasContent(dirName) || treePath == null) {
            return;
        }
        if (treePath.length == 1) {
            final Node tNode = node.getNode(treePath[0]);
            if (tNode == null) {
                final Node newDirNode = Node.builder().name(treePath[0]).type(NodeType.DIRECTORY).build();
                newDirNode.add(Node.builder().name(dirName).type(NodeType.DIRECTORY).zip(zip).build());
                node.add(newDirNode);
            } else {
                tNode.add(Node.builder().name(dirName).type(NodeType.DIRECTORY).zip(zip).build());
            }
        } else {
            touchDirectory(node.getNode(treePath[0]), dirName, zip, Arrays.copyOfRange(treePath, 1, treePath.length));
        }
    }

    public Node getNode(final Node node, final String... treePath) {
        if (node == null) {
            return null;
        }
        if (treePath == null) {
            return node;
        }
        if (treePath.length == 1) {
            if (node.getName().equals(treePath[0])) {
                return node;
            }
            return node.getNode(treePath[0]);
        } else {
            if (node.getName().equals(treePath[0])) {
                return getNode(node, Arrays.copyOfRange(treePath, 1, treePath.length));
            }
            return getNode(node.getNode(treePath[0]), Arrays.copyOfRange(treePath, 1, treePath.length));
        }
    }

    public String getRootName() {
        if (this.root == null) {
            return null;
        }
        return this.root.getName();
    }


    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public static class WorkbookFileSystemBuilder {
        @SuppressWarnings("all")
        private Node root;

        @SuppressWarnings("all")
        WorkbookFileSystemBuilder() {
        }

        @SuppressWarnings("all")
        public WorkbookFileSystem.WorkbookFileSystemBuilder root(final Node root) {
            this.root = root;
            return this;
        }

        @SuppressWarnings("all")
        public WorkbookFileSystem build() {
            return new WorkbookFileSystem(this.root);
        }

        @Override
        @SuppressWarnings("all")
        public String toString() {
            return "WorkbookFileSystem.WorkbookFileSystemBuilder(root=" + this.root + ")";
        }
    }

    @SuppressWarnings("all")
    public static WorkbookFileSystem.WorkbookFileSystemBuilder builder() {
        return new WorkbookFileSystemBuilder();
    }

    @SuppressWarnings("all")
    public Node getRoot() {
        return this.root;
    }

    @SuppressWarnings("all")
    public void setRoot(final Node root) {
        this.root = root;
    }

    @SuppressWarnings("all")
    public WorkbookFileSystem() {
    }

    @SuppressWarnings("all")
    public WorkbookFileSystem(final Node root) {
        this.root = root;
    }
    //</editor-fold>
}
