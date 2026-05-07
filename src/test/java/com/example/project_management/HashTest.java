package com.example.project_management;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashTest {
    @Test
    public void generateHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        System.out.println("HASH_START|" + encoder.encode("123456") + "|HASH_END");
    }
}
