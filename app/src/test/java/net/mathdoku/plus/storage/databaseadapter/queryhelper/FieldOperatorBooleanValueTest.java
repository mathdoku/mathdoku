package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FieldOperatorBooleanValueTest {
    @Test
    public void createFieldOperatorBooleanValue_Success() throws Exception {
        assertThat(new FieldOperatorBooleanValue("FIELD", FieldOperatorValue.Operator.EQUALS, true).toString(),
                   is("`FIELD` = 'true'"));
    }
}
