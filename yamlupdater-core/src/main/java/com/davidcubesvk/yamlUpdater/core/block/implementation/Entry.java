package com.davidcubesvk.yamlUpdater.core.block.implementation;

import com.davidcubesvk.yamlUpdater.core.block.Block;
import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.nodes.Node;

/**
 * Represents one YAML <i>mapping</i> (key=value pair), while storing the mapping's value and comments.
 */
public class Entry extends Block<Object> {

    /**
     * Creates a mapping using the given parameters; while storing references to comments from the given nodes.
     *
     * @param keyNode   key node of the mapping
     * @param valueNode value node of the mapping
     * @param value     the value to store
     */
    public Entry(@Nullable Node keyNode, @Nullable Node valueNode, @Nullable Object value) {
        super(keyNode, valueNode, value);
    }

    /**
     * Creates a mapping with the same comments as the provided previous block, with the given value. If given block is
     * <code>null</code>, creates a mapping with no comments.
     *
     * @param previous the previous block to reference comments from
     * @param value    the value to store
     */
    public Entry(@Nullable Block<?> previous, @Nullable Object value) {
        super(previous, value);
    }
}