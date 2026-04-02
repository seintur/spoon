package spoon.test.comment;

import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.declaration.CtClass;
import spoon.testing.utils.BySimpleName;
import spoon.testing.utils.ModelTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static spoon.testing.assertions.SpoonAssertions.assertThat;

class MarkdownCommentTest {

	final static private String SIMPLE_CODE_WITH_MARKDOWN_DOCUMENTATION = """
		class MarkdownComment {
		    /// A Markdown comment
		    ///\s
		    /// @param i
		    /// 		an integer
		    /// @return the next integer
		    int next(int i) {
		        return i + 1;
		    }
		}""";

	@ModelTest(code = SIMPLE_CODE_WITH_MARKDOWN_DOCUMENTATION, complianceLevel = 23)
	void testMarkdownComment23OrLatter(@BySimpleName("MarkdownComment") CtClass<?> ctClass) {
		// contract: starting with JDK 23 /// starting lines are Markdown documentation
		assertThat(ctClass.getMethodsByName("next").get(0)).getComments().hasSize(1);
		CtComment ctComment = ctClass.getMethodsByName("next").get(0).getComments().get(0);
		assertThat(ctComment).getCommentType().isEqualTo(CtComment.CommentType.MARKDOWN);
		assertThat(ctComment.asJavaDoc()).getTags().hasSize(2);
		assertEquals(SIMPLE_CODE_WITH_MARKDOWN_DOCUMENTATION, ctClass.toString());
	}

	@ModelTest(code = SIMPLE_CODE_WITH_MARKDOWN_DOCUMENTATION, complianceLevel = 22)
	void testMarkdownCommentBefore23(@BySimpleName("MarkdownComment") CtClass<?> ctClass) {
		// contract: prior to JDK 23 /// starting lines are inline comments
		assertThat(ctClass.getMethodsByName("next").get(0)).getComments().hasSize(5);
		List<CtComment> ctComments = ctClass.getMethodsByName("next").get(0).getComments();
		for (var ctComment : ctComments) {
			assertThat(ctComment).getCommentType().isEqualTo(CtComment.CommentType.INLINE);
		}
	}

	@ModelTest(value = "src/test/java/spoon/test/comment/testclasses/WildComments23.java", complianceLevel = 23)
	void testWildComments23OrLatter(@BySimpleName("WildComments23") CtClass<?> type) {
		// contract: tests that value of comment is correct even for wild combinations of characters starting with JDK 23
		List<CtLiteral<String>> literals = (List) ((CtNewArray<?>) type.getField("comments").getDefaultExpression()).getElements();
		assertEquals(43, literals.size());
		for (CtLiteral<String> literal : literals) {
			assertEquals(1, literal.getComments().size());
			CtComment comment = literal.getComments().get(0);
			String expected = literal.getValue();
			assertEquals(expected, comment.getContent(), literal.getPosition().toString());
		}
	}

	@ModelTest(value = "src/test/java/spoon/test/comment/testclasses/WildComments.java", complianceLevel = 22)
	void testWildComments(@BySimpleName("WildComments") CtClass<?> type) {
		// contract: tests that value of comment is correct even for wild combinations of characters before JDK 23
		List<CtLiteral<String>> literals = (List) ((CtNewArray<?>) type.getField("comments").getDefaultExpression()).getElements();
		assertEquals(43, literals.size());
		for (CtLiteral<String> literal : literals) {
			assertEquals(1, literal.getComments().size());
			CtComment comment = literal.getComments().get(0);
			String expected = literal.getValue();
			assertEquals(expected, comment.getContent(), literal.getPosition().toString());
		}
	}
}
