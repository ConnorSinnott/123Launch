package com.pluviostudios.dialin.data;

import android.content.Context;
import android.util.Log;

import com.pluviostudios.dialin.action.Action;
import com.pluviostudios.dialin.action.ActionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by spectre on 8/2/16.
 */
public class JSONNodeConverter {

    public static final String TAG = "JSONNodeConverter";

    public static final String JSON_ACTION_ID = "json_action_id";
    public static final String JSON_ACTION_PARAMS = "json_action_params";
    public static final String JSON_CHILDREN = "json_children";

    public static void saveNodeTree(Context context, long configId, Node root) {

        try {

            // Create the top level container for all JSON
            JSONObject jSONContainer = new JSONObject();

            // Convert the node tree to json
            JSONObject rootJSONObject = convertNodeToJSON(root);

            String dataToSave = rootJSONObject.toString();
            FileManager.writeToFile(context, String.valueOf(configId), dataToSave);

        } catch (JSONException e) {
            Log.e(TAG, "saveNodeTree: Saving Failed", e);
        } catch (IOException e) {
            Log.e(TAG, "saveNodeTree: Saving failed", e);
        }

    }

    // Used by activities. This will recreate the entire node tree and load all action arguments
    public static Node loadNodeTree(Context context, long configId) {

        try {

            String savedData = FileManager.readFromFile(context, String.valueOf(configId));
            JSONObject jsonObject = new JSONObject(savedData);

            return convertJSONToNode(null, jsonObject, false);

        } catch (JSONException e) {
            Log.e(TAG, "loadNodeByPath: Load Failed", e);
        } catch (IOException e) {
            Log.e(TAG, "loadNodeTree: Load Failed", e);
        }

        return new Node();

    }

    // Used by the widget. This will not be recursive, it will just get info about a specific node and its immediate children
    public static Node loadNodeByPath(Context context, String name, ArrayList<Integer> path) {

        return new Node();

    }

    private static Node convertJSONToNode(Node parent, JSONObject object, boolean preview) throws JSONException {

        Node currentNode = new Node();
        currentNode.parent = parent;

        // If the current node should have an action
        if (object.has(JSON_ACTION_ID)) {

            // Get Action
            int actionId = object.getInt(JSON_ACTION_ID);
            Action action = ActionManager.getInstanceOfAction(actionId);

            // If the action has parameters
            if (object.has(JSON_ACTION_PARAMS)) {

                // Get action parameters
                JSONArray jsonActionParameters = object.getJSONArray(JSON_ACTION_PARAMS);
                ArrayList<String> actionParameters = new ArrayList<>();

                for (int i = 0; i < jsonActionParameters.length(); i++) {
                    actionParameters.add((String) jsonActionParameters.get(i));
                }

                // Assign action parameters
                action.actionArguments = actionParameters;

            }

            // Assign action to node
            currentNode.setAction(action);

        }

        if (object.has(JSON_CHILDREN)) {

            JSONObject jsonChildren = object.getJSONObject(JSON_CHILDREN);
            Iterator<String> jsonChildIndexes = jsonChildren.keys();

            // Iterate though all child indexes
            while (jsonChildIndexes.hasNext()) {

                String currentChildIndex = jsonChildIndexes.next();
                JSONObject jsonChildObject = jsonChildren.getJSONObject(currentChildIndex);

                if (preview) {
                    // Generate a preview of the children without recursion

                    Node newChild = currentNode.getChild(Integer.parseInt(currentChildIndex));

                    if (jsonChildObject.has(JSON_ACTION_ID)) {
                        int actionId = jsonChildObject.getInt(JSON_ACTION_ID);
                        Action action = ActionManager.getInstanceOfAction(actionId);
                        newChild.setAction(action);
                    }

                } else {
                    // Recursively add all children to the parent node

                    Node newChild = convertJSONToNode(currentNode, jsonChildObject, false);
                    currentNode.setChild(Integer.parseInt(currentChildIndex), newChild);

                }

            }

        }

        return currentNode;

    }

    private static JSONObject convertNodeToJSON(Node node) throws JSONException {

        JSONObject currentNode = new JSONObject();

        ////////////////////////////////////////////////
        // Saving Actions
        ////////////////////////////////////////////////

        // If node has an action
        if (node.hasAction()) {

            Action action = node.getAction();

            // Add the action to the passed json object
            currentNode.put(JSON_ACTION_ID, action.id);

            // If the action has arguments, add them to a JSONArray
            if (action.actionArguments != null) {

                JSONArray actionArguments = new JSONArray();

                for (String currActionArgument : action.actionArguments) {
                    actionArguments.put(currActionArgument);
                }

                // Add parameter array to json object
                currentNode.put(JSON_ACTION_PARAMS, actionArguments);

            }

        }

        ////////////////////////////////////////////////
        // Saving Children (Recursive call below)
        ////////////////////////////////////////////////

        // Get the indexes for children attached to current node
        Integer[] childIndexes = node.getChildIndexes();

        // If there are any children....
        if (childIndexes.length > 0) {

            // Create an object to house the children.
            // I decided to not use a JSONArray so that I can name the children with their location
            // This should make retrieval easier down the road
            JSONObject childrenObject = new JSONObject();

            // Loop through all known children indexes
            for (Integer currentIndex : childIndexes) {

                // Get the child node
                Node childNode = node.getChild(currentIndex);

                // Only add the child node if it is not blank
                if (!node.getChild(currentIndex).isBlank) {

                    // The recursive call!!! Down the rabbit hole this goes
                    JSONObject newChildObject = convertNodeToJSON(childNode);

                    childrenObject.put(String.valueOf(currentIndex), newChildObject);

                }

            }

            currentNode.put(JSON_CHILDREN, childrenObject);

        }

        return currentNode;

    }

}
