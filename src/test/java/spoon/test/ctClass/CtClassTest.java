package spoon.test.ctClass;

import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.test.TestUtils;
import spoon.test.ctClass.testclasses.Foo;
import spoon.test.ctClass.testclasses.Pozole;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static spoon.test.TestUtils.buildClass;

public class CtClassTest {

	@Test
	public void getConstructor() throws Exception {
		final Factory build = TestUtils.build(Foo.class);
		final CtClass<?> foo = (CtClass<?>) build.Type().get(Foo.class);

		assertEquals(3, foo.getConstructors().size());

		CtTypeReference<Object> typeString = build.Code().createCtTypeReference(String.class);
		CtConstructor<?> constructor = foo.getConstructor(typeString);
		assertEquals(typeString, constructor.getParameters().get(0).getType());

		CtArrayTypeReference<Object> typeStringArray = build.Core().createArrayTypeReference();
		typeStringArray.setComponentType(typeString);
		constructor = foo.getConstructor(typeStringArray);
		assertEquals(typeStringArray, constructor.getParameters().get(0).getType());

		CtArrayTypeReference<Object> typeStringArrayArray = build.Core().createArrayTypeReference();
		typeStringArrayArray.setComponentType(typeStringArray);
		constructor = foo.getConstructor(typeStringArrayArray);
		assertEquals(typeStringArrayArray, constructor.getParameters().get(0).getType());
	}

	@Test
	public void testParentOfTheEnclosingClassOfStaticClass() throws Exception {
		// contract: When we have a static class which extends a superclass in the classpath,
		// the enclosing class don't have a superclass. This is probably a bug in JDT but good
		// luck to report a bug about noclasspath in their bugtracker. :)

		final Launcher launcher = new Launcher();
		launcher.addInputResource("./src/test/resources/noclasspath/InvariantChecker.java");
		launcher.addInputResource("./src/test/resources/noclasspath/FileIO.java");
		launcher.addInputResource("./src/test/resources/noclasspath/Daikon.java");
		launcher.setSourceOutputDirectory("./target/class");
		launcher.getEnvironment().setNoClasspath(true);
		launcher.run();

		final CtClass<Object> aClass = launcher.getFactory().Class().get("daikon.tools.InvariantChecker");

		final CtType<?> staticClass = aClass.getNestedType("InvariantCheckProcessor");
		assertNotNull(staticClass);
		assertEquals("InvariantCheckProcessor", staticClass.getSimpleName());
		assertNotNull(staticClass.getSuperclass());
		assertEquals("daikon.FileIO$Processor", staticClass.getSuperclass().getQualifiedName());
		assertNull(aClass.getSuperclass());

		TestUtils.canBeBuilt("./target/class", 8, true);
	}

	@Test
	public void testNoClasspathWithSuperClassOfAClassInAnInterface() throws Exception {
		// contract: When we specify a superclass which is declared in an interface and
		// where the visibility is okay, we must use it.

		final Launcher launcher = new Launcher();
		launcher.addInputResource("./src/test/resources/noclasspath/draw2d");
		launcher.setSourceOutputDirectory("./target/draw2d");
		launcher.getEnvironment().setNoClasspath(true);
		launcher.run();

		final CtClass<Object> aClass = launcher.getFactory().Class().get("org.eclipse.draw2d.parts.ScrollableThumbnail");
		final CtType<?> innerClass = aClass.getNestedType("ClickScrollerAndDragTransferrer");
		assertEquals("org.eclipse.draw2d.MouseMotionListener$Stub", innerClass.getSuperclass().getQualifiedName());

		TestUtils.canBeBuilt("./target/draw2d", 8, true);
	}

	@Test
	public void testAllTypeReferencesToALocalTypeShouldNotStartWithNumber() throws Exception {
		// contract: When we have a local type, we should never start with its number. But this
		// number isn't rewrite only in the class declaration but in all type references and in
		// the constructor.
		final CtType<Pozole> aPozole = buildClass(Pozole.class);
		final CtClass<?> cook = aPozole.getNestedType("1Cook");

		assertEquals("1Cook", cook.getSimpleName());
		assertEquals("spoon.test.ctClass.testclasses.Pozole$1Cook", cook.getQualifiedName());
		final Set<? extends CtConstructor<?>> constructors = cook.getConstructors();
		final String expectedConstructor = "public Cook() {" + System.lineSeparator() + "}";
		assertEquals(expectedConstructor, constructors.toArray(new CtConstructor[constructors.size()])[0].toString());
		assertEquals("final java.lang.Class<Cook> cookClass = Cook.class", cook.getMethod("m").getBody().getStatement(0).toString());
	}
}
