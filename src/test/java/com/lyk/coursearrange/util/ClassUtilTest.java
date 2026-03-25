package com.lyk.coursearrange.util;

import com.lyk.coursearrange.common.ConstantInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassUtilTest {

    @Test
    void cutGene_shouldReturnFullStandardClassroomCodeTail() {
        String gene = "12525010001T00011000010101B08-302";

        assertEquals("B08-302", ClassUtil.cutGene(ConstantInfo.CLASSROOM_NO, gene));
    }
}
