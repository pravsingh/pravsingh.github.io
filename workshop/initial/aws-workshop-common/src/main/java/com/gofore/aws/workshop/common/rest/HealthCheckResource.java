package com.gofore.aws.workshop.common.rest;

import java.util.Map;

import com.gofore.aws.workshop.common.functional.Maps;
import org.restlet.resource.Get;

public class HealthCheckResource extends RestServerResource {

    @Get("json")
    public Map<String, String> healthcheck() {
        return Maps.of("all", "ok");
    }
}
