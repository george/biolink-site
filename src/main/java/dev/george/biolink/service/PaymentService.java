package dev.george.biolink.service;

import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public void handleCompletion(JsonObject metadata) {
        int packageId = metadata.get("packageId").getAsInt();
        int profileId = metadata.get("profileId").getAsInt();
    }

}
