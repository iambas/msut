package com.darker.motorservice;

import com.darker.motorservice.utils.StringUtils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Darker on 14/11/60.
 */

public class StringUtilsTest {
    @Test
    public void stringNotNull_isCorrect(){
        Assert.assertTrue(StringUtils.stringOk("1234"));
    }

    @Test
    public void stringNull_isNotCorrect(){
        Assert.assertFalse(StringUtils.stringOk(null));
    }

    @Test
    public void stringEmpty_isNotCorrect(){
        Assert.assertFalse(StringUtils.stringOk(""));
    }

    @Test
    public void phoneNumber_9digit_isCorrect(){
        Assert.assertTrue(StringUtils.isPhoneNumber("123456789"));
    }

    @Test
    public void phoneNumber_10digit_isCorrect(){
        Assert.assertTrue(StringUtils.isPhoneNumber("1234567890"));
    }

    @Test
    public void phoneNumber_moreThan10digit_isNotCorrect(){
        Assert.assertFalse(StringUtils.isPhoneNumber("12345678901"));
    }

    @Test
    public void phoneNumber_lessThan9digit_isNotCorrect(){
        Assert.assertFalse(StringUtils.isPhoneNumber("12345678"));
    }

    @Test
    public void phoneNumber_textString_isNotCorrect(){
        Assert.assertFalse(StringUtils.isPhoneNumber("test"));
    }

    @Test
    public void phoneNumber_textEmpty_isNotCorrect(){
        Assert.assertFalse(StringUtils.isPhoneNumber(""));
    }

    @Test
    public void phoneNumberNull_isNotCorrect(){
        Assert.assertFalse(StringUtils.isPhoneNumber(null));
    }

    @Test
    public void email_isCorrect(){
        String email = "test@mail.com";
        Assert.assertTrue(StringUtils.isEmail(email));
    }

    @Test
    public void email_null_isNotCorrect(){
        String email = null;
        Assert.assertFalse(StringUtils.isEmail(email));
    }

    @Test
    public void email_textEmpty_isNotCorrect(){
        String email = "";
        Assert.assertFalse(StringUtils.isEmail(email));
    }

    @Test
    public void email_normalText_isNotCorrect(){
        String email = "qweqw";
        Assert.assertFalse(StringUtils.isEmail(email));
    }

    @Test
    public void email_wrongFormat_isNotCorrect(){
        String email = "a@mail";
        Assert.assertFalse(StringUtils.isEmail(email));
    }

    @Test
    public void email_wrongFormatWithDot_isNotCorrect(){
        String email = "a@mail.";
        Assert.assertFalse(StringUtils.isEmail(email));
    }

    @Test
    public void email_notAt_isNotCorrect(){
        String email = "mail.com";
        Assert.assertFalse(StringUtils.isEmail(email));
    }
}
