package com.CHANGEME;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

import com.CHANGEME.shared.application.UseCase;
import com.CHANGEME.shared.domain.DomainException;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

/** Rules described in CLAUDE.md and .claude/rules. If one fails, fix the code. */
@AnalyzeClasses(
    packagesOf = ArchitectureTest.class,
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  // ---- Dependency rule -----------------------------------------------------------------

  @ArchTest
  static final ArchRule domainDependsOnlyOnJavaAndDomain =
      classes()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage("java..", "..domain..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule applicationDependsOnlyOnJavaDomainAndApplication =
      classes()
          .that()
          .resideInAPackage("..application..")
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage("java..", "..domain..", "..application..")
          .allowEmptyShould(true);

  // ---- Use cases ------------------------------------------------------------------------

  @ArchTest
  static final ArchRule useCasesLiveInApplicationAndAreNamedService =
      classes()
          .that()
          .areAnnotatedWith(UseCase.class)
          .should()
          .resideInAPackage("..application..")
          .andShould()
          .haveSimpleNameEndingWith("Service")
          .andShould()
          .haveModifier(JavaModifier.PUBLIC)
          .andShould()
          .notHaveModifier(JavaModifier.FINAL)
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule servicesInApplicationAreUseCases =
      classes()
          .that()
          .resideInAPackage("..application..")
          .and()
          .haveSimpleNameEndingWith("Service")
          .should()
          .beAnnotatedWith(UseCase.class)
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule useCasesHaveASinglePublicMethod =
      classes()
          .that()
          .areAnnotatedWith(UseCase.class)
          .should(haveExactlyOnePublicMethod())
          .allowEmptyShould(true);

  // ---- Repositories ---------------------------------------------------------------------

  @ArchTest
  static final ArchRule repositoryInterfacesLiveInDomain =
      classes()
          .that()
          .haveSimpleNameEndingWith("Repository")
          .and()
          .areInterfaces()
          .should()
          .resideInAPackage("..domain..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule repositoryImplementationsLiveInPersistence =
      classes()
          .that()
          .haveSimpleNameEndingWith("Repository")
          .and()
          .areNotInterfaces()
          .should()
          .resideInAPackage("..infrastructure.persistence..")
          .allowEmptyShould(true);

  // ---- Web adapters ---------------------------------------------------------------------

  @ArchTest
  static final ArchRule controllersLiveInWeb =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .resideInAPackage("..infrastructure.web..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule webDoesNotBypassUseCases =
      noClasses()
          .that()
          .resideInAPackage("..infrastructure.web..")
          .should()
          .dependOnClassesThat()
          .haveSimpleNameEndingWith("Repository")
          .orShould()
          .dependOnClassesThat()
          .resideInAPackage("..infrastructure.persistence..")
          .allowEmptyShould(true);

  // Code generated from api.yaml: only web adapters use it.
  @ArchTest
  static final ArchRule generatedApiIsUsedOnlyByWebAdapters =
      noClasses()
          .that()
          .resideOutsideOfPackage("..infrastructure.web..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..shared.infrastructure.web.api..")
          .allowEmptyShould(true);

  // ---- Java style -----------------------------------------------------------------------

  @ArchTest
  static final ArchRule domainExceptionsExtendDomainException =
      classes()
          .that()
          .resideInAPackage("..domain..")
          .and()
          .areAssignableTo(RuntimeException.class)
          .should()
          .beAssignableTo(DomainException.class)
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule domainUsesRecordStyleAccessors =
      noMethods()
          .that()
          .areDeclaredInClassesThat()
          .resideInAPackage("..domain..")
          .should()
          .haveNameMatching("get[A-Z].*")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule optionalIsNeverAField =
      noFields().should().haveRawType(Optional.class).allowEmptyShould(true);

  // ---- Spring style ---------------------------------------------------------------------

  @ArchTest static final ArchRule noFieldInjection = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

  @ArchTest
  static final ArchRule noValueProfileOrTransactional =
      noClasses()
          .should()
          .dependOnClassesThat()
          .belongToAnyOf(Value.class, Profile.class, Transactional.class)
          .allowEmptyShould(true);

  private static ArchCondition<JavaClass> haveExactlyOnePublicMethod() {
    return new ArchCondition<JavaClass>("have exactly one public method") {
      @Override
      public void check(final JavaClass javaClass, final ConditionEvents events) {
        final long publicMethods =
            javaClass.getMethods().stream()
                .filter(method -> method.getModifiers().contains(JavaModifier.PUBLIC))
                .filter(method -> !method.getModifiers().contains(JavaModifier.STATIC))
                .count();
        if (publicMethods != 1) {
          events.add(
              SimpleConditionEvent.violated(
                  javaClass,
                  javaClass.getName() + " has " + publicMethods + " public methods, expected 1"));
        }
      }
    };
  }
}
