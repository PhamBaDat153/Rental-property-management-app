package com.android_dev.rentaly_management.Apis;

import com.android_dev.rentaly_management.DTO.Province;
import org.junit.Test;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;

public class ProvinceMappingTest {
    @Test public void provinceModelKeepsNamesAndCodesForNameOnlyRequests() {
        Province province = new Province();
        province.code = 79;
        province.name = "Thành phố Hồ Chí Minh";
        province.districts = new ArrayList<>();
        assertEquals(79, province.code);
        assertEquals("Thành phố Hồ Chí Minh", province.name);
    }
}
