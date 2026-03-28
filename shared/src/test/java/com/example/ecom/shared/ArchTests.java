package com.projects.ecom.shared;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** ArchUnit boundary tests to enforce modular boundaries. */
public class ArchTests {
  @Test
  void catalog_should_not_access_other_modules_directly() {
    JavaClasses imports = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
        .importPackages("com.projects.ecom");

    ArchRule rule1 = noClasses()
        .that().resideInAPackage("com.projects.ecom.catalog..")
        .should().accessClassesThat().resideInAPackage("com.projects.ecom.user..");

    ArchRule rule2 = noClasses()
        .that().resideInAPackage("com.projects.ecom.catalog..")
        .should().accessClassesThat().resideInAPackage("com.projects.ecom.order..");

    ArchRule rule3 = noClasses()
        .that().resideInAPackage("com.projects.ecom.catalog..")
        .should().accessClassesThat().resideInAPackage("com.projects.ecom.cart..");

    ArchRule rule4 = noClasses()
        .that().resideInAPackage("com.projects.ecom.catalog..")
        .should().accessClassesThat().resideInAPackage("com.projects.ecom.payment..");

    rule1.check(imports);
    rule2.check(imports);
    rule3.check(imports);
    rule4.check(imports);
  }
}
