package com.desecratedtree.quill.codec.item;

import com.desecratedtree.quill.defs.ItemDefinitions;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemEncoderTest {

    @Test
    void paramsAreSerializedWhenOpcodeWasNotPreviouslyPresent() {
        ItemDefinitions original = new ItemDefinitions(123, false);
        original.itemParams = new HashMap<>();
        original.itemParams.put(0x010203, 42);
        original.itemParams.put(0x040506, "hello");

        ItemDefinitions decoded = new ItemDefinitions(123, false);
        decoded.readOpcodeValues(new com.desecratedtree.quill.codec.InputStream(ItemEncoder.encode(original)));

        assertEquals(42, decoded.itemParams.get(0x010203));
        assertEquals("hello", decoded.itemParams.get(0x040506));
        assertTrue(decoded.presentOpcodeSet.contains(249));
    }
}
