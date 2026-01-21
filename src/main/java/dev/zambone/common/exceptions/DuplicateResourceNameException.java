package dev.zambone.common.exceptions;

public class DuplicateResourceNameException extends RuntimeException {

  private final String resourceType;
  private final String fieldName;
  private final String value;

  public DuplicateResourceNameException(String resourceType, String fieldName, String value) {
    super(String.format("%s with %s '%s' already exists", resourceType, fieldName, value));
    this.resourceType = resourceType;
    this.fieldName = fieldName;
    this.value = value;
  }

  public String getResourceType() {
    return resourceType;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getValue() {
    return value;
  }
}
