package com.darker.motorservice;

import com.darker.motorservice.utils.StringUtils;

import org.junit.Assert;
import org.junit.Test;

import static com.darker.motorservice.utils.StringUtils.isPhoneNumber;
import static com.darker.motorservice.utils.StringUtils.stringOk;

/**
 * Created by Darker on 14/11/60.
 */

public class StringUtilsTest {
    @Test
    public void stringNotNull_isCurrect(){
        Assert.assertTrue(stringOk("1234"));
    }

    @Test
    public void stringNull_isNotCurrect(){
        Assert.assertFalse(stringOk(null));
    }

    @Test
    public void stringEmpty_isNotCurrect(){
        Assert.assertFalse(stringOk(""));
    }

    @Test
    public void phoneNumber_9digit_isCurrect(){
        Assert.assertTrue(StringUtils.isPhoneNumber("123456789"));
    }

    @Test
    public void phoneNumber_10digit_isCurrect(){
        Assert.assertTrue(isPhoneNumber("1234567890"));
    }

    @Test
    public void phoneNumber_moreThan10digit_isNotCurrect(){
        Assert.assertFalse(isPhoneNumber("12345678901"));
    }

    @Test
    public void phoneNumber_lessThan9digit_isNotCurrect(){
        Assert.assertFalse(isPhoneNumber("12345678"));
    }

    @Test
    public void phoneNumber_textString_isNotCurrect(){
        Assert.assertFalse(isPhoneNumber("test"));
    }

    @Test
    public void phoneNumber_textEmpty_isNotCurrect(){
        Assert.assertFalse(isPhoneNumber(""));
    }

    @Test
    public void phoneNumberNull_isNotCurrect(){
        Assert.assertFalse(isPhoneNumber(null));
    }
}
