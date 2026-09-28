package com.beacepl.service_request_service.enums;

/**
 * ADD    - create a new item in a list field on the account (e.g. add a nominee)
 * EDIT   - update simple fields, or update an existing item in a list field
 * REMOVE - remove an item from a list field (e.g. remove a nominee)
 */
public enum RequestAction {
    ADD,
    EDIT,
    REMOVE
}
