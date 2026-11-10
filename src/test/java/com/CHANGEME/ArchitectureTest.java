package com.CHANGEME;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

import com.CHANGEME.common.application.UseCase;
import com.CHANGEME.common.domain.exception.DomainException;
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

/** Rules described in doc/ai/architecture.md. If one fails, fix the code. */
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
  static final ArchRule useCasesLiveInUsecasesAndAreNamedService =
      classes()
          .that()
          .areAnnotatedWith(UseCase.class)
          .should()
          .resideInAPackage("..application.usecases..")
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
          .resideInAPackage("..domain.repository..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule repositoryImplementationsLiveInPersistence =
      classes()
          .that()
          .haveSimpleNameEndingWith("Repository")
          .and()
          .areNotInterfaces()
          .should()
          .resideInAPackage("..infrastructure.persistence.mybatis..")
          .allowEmptyShould(true);

  // ---- Web adapters ---------------------------------------------------------------------

  @ArchTest
  static final ArchRule controllersLiveInRestController =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .resideInAPackage("..infrastructure.rest.controller..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule restDoesNotBypassUseCases =
      noClasses()
          .that()
          .resideInAPackage("..infrastructure.rest..")
          .should()
          .dependOnClassesThat()
          .haveSimpleNameEndingWith("Repository")
          .orShould()
          .dependOnClassesThat()
          .resideInAPackage("..infrastructure.persistence..")
          .allowEmptyShould(true);

  // Code generated from api.yaml: only rest adapters use it.
  @ArchTest
  static final ArchRule generatedApiIsUsedOnlyByRestAdapters =
      noClasses()
          .that()
          .resideOutsideOfPackage("..infrastructure.rest..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..infrastructure.rest.api..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule restConversionsLiveInRestMapper =
      classes()
          .that()
          .haveSimpleNameEndingWith("ApiConversion")
          .should()
          .resideInAPackage("..infrastructure.rest.mapper..")
          .allowEmptyShould(true);

  // ---- Package structure (doc/ai/architecture.md) -----------------------------------------

  @ArchTest
  static final ArchRule commandsLiveInDtoCommands =
      classes()
          .that()
          .haveSimpleNameEndingWith("Command")
          .should()
          .resideInAPackage("..application.dto.commands..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule dtoCommandsAreNamedCommand =
      classes()
          .that()
          .resideInAPackage("..application.dto.commands..")
          .should()
          .haveSimpleNameEndingWith("Command")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule queriesLiveInDtoQueries =
      classes()
          .that()
          .haveSimpleNameEndingWith("Query")
          .should()
          .resideInAPackage("..application.dto.queries..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule dtoQueriesAreNamedQuery =
      classes()
          .that()
          .resideInAPackage("..application.dto.queries..")
          .should()
          .haveSimpleNameEndingWith("Query")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule applicationMappersLiveInMapper =
      classes()
          .that()
          .resideInAPackage("..application..")
          .and()
          .haveSimpleNameEndingWith("Mapper")
          .should()
          .resideInAPackage("..application.mapper..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule domainExceptionsLiveInExceptionPackage =
      classes()
          .that()
          .resideInAPackage("..domain..")
          .and()
          .areAssignableTo(DomainException.class)
          .should()
          .resideInAPackage("..domain.exception..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule domainServicesLiveInServices =
      classes()
          .that()
          .resideInAPackage("..domain..")
          .and()
          .haveSimpleNameEndingWith("Service")
          .should()
          .resideInAPackage("..domain.services..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule policiesLiveInPolicies =
      classes()
          .that()
          .haveSimpleNameEndingWith("Policy")
          .should()
          .resideInAPackage("..domain.policies..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule guardsLiveInGuards =
      classes()
          .that()
          .haveSimpleNameEndingWith("Guard")
          .should()
          .resideInAPackage("..domain.guards..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule persistenceRowsLiveInRow =
      classes()
          .that()
          .haveSimpleNameEndingWith("Row")
          .should()
          .resideInAPackage("..infrastructure.persistence.mybatis.row..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule persistenceMappersLiveInMapper =
      classes()
          .that()
          .resideInAPackage("..infrastructure.persistence..")
          .and()
          .haveSimpleNameEndingWith("Mapper")
          .should()
          .resideInAPackage("..infrastructure.persistence.mybatis.mapper..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule persistenceMappingsLiveInMapping =
      classes()
          .that()
          .haveSimpleNameEndingWith("Mapping")
          .should()
          .resideInAPackage("..infrastructure.persistence.mybatis.mapping..")
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
