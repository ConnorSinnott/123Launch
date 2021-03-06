package com.pluviostudios.onetwothreelaunch.data;

import com.pluviostudios.onetwothreelaunch.action.Action;

import java.util.HashMap;

/**
 * Created by spectre on 7/26/16.
 */
public class Node {

    public static final String TAG = "Node";

    public boolean isBlank = true;
    public Node parent;
    private HashMap<Integer, Node> mChildren;
    private Action mAction;

    public Node() {
        this(null);
    }

    public Node(Node parent) {
        this.parent = parent;
        mChildren = new HashMap<>();
    }

    /**
     * The idea is that the tree can can traversed down forever, but unless the user saves an action,
     * these new nodes will be removed. This will simplify much of the programming without hindering
     * its functionality.
     */
    public Node getChild(int index) {

        Node child = mChildren.get(index);
        if (child != null) {
            return child;
        } else {
            Node newNode = new Node(this);
            mChildren.put(index, newNode);
            return newNode;
        }

    }

    public void setChild(int index, Node node) {
        mChildren.put(index, node);
    }

    public Action getAction() {
        return mAction;
    }

    public boolean hasAction() {
        return mAction != null;
    }

    /**
     * So as soon as the user sets an action, traverse through all the parent nodes and change
     * isBlank to false. Since they actually lead somewhere, they should not be marked for deletion.
     */
    public void setAction(Action dialinAction) {

        mAction = dialinAction;

        if (dialinAction != null) {

            Node currentNode = this;
            while (currentNode != null) {
                currentNode.isBlank = false;
                currentNode = currentNode.parent;
            }

        } else {

            Node currentNode = this;
            while (currentNode != null) {
                if (currentNode.getChildIndexes().length == 1) {
                    currentNode.isBlank = true;
                    currentNode = currentNode.parent;
                } else {
                    break;
                }
            }

        }

    }

    public Integer[] getChildIndexes() {
        return mChildren.keySet().toArray(new Integer[mChildren.keySet().size()]);
    }

}
