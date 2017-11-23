package com.darker.motorservice;

import org.junit.Assert;
import org.junit.Test;

import static com.darker.motorservice.utils.StringUtils.stringOk;

/**
 * Created by Darker on 14/11/60.
 */

public class StringUtilsTest {
    @Test
    public void stringNotNull_isTrue(){
        Assert.assertTrue(stringOk("1234"));
    }
}
