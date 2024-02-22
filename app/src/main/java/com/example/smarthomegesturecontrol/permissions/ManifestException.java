package com.example.smarthomegesturecontrol.permissions;

final class ManifestException extends RuntimeException {

    ManifestException() {
        super("Permissions are not in thw Manifest file");
    }

    ManifestException(String permission) {
        super(permission + ": Permissions are not in thw Manifest file");
    }
}