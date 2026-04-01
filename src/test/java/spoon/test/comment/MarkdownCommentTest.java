package spoon.test.comment;

import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.declaration.CtClass;
import spoon.testing.utils.BySimpleName;
import spoon.testing.utils.ModelTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static spoon.testing.assertions.SpoonAssertions.assertThat;

class MarkdownCommentTest {

	final static private String CODE = """
		class MarkdownComment {
		    /// A Markdown comment
		    ///
		    /// @param i an integer
		    /// @return  the next integer
		    int next(int i) {
		        return i + 1;
		    }
		}""";

	@ModelTest(code = CODE, complianceLevel = 23)
	void testMarkdownComment23OrLatter(@BySimpleName("MarkdownComment") CtClass<?> ctClass) {
		assertThat(ctClass.getMethodsByName("next").get(0)).getComments().hasSize(1);
		CtComment ctComment = ctClass.getMethodsByName("next").get(0).getComments().get(0);
		assertThat(ctComment).getCommentType().isEqualTo(CtComment.CommentType.MARKDOWN);
		assertEquals(CODE, ctClass.toString());
	}

	@ModelTest(code = CODE, complianceLevel = 22)
	void testMarkdownCommentBefore23(@BySimpleName("MarkdownComment") CtClass<?> ctClass) {
		assertThat(ctClass.getMethodsByName("next").get(0)).getComments().hasSize(4);
		List<CtComment> ctComments = ctClass.getMethodsByName("next").get(0).getComments();
		for (var ctComment : ctComments) {
			assertThat(ctComment).getCommentType().isEqualTo(CtComment.CommentType.INLINE);
		}
	}

	@ModelTest(value = "src/test/java/spoon/test/comment/testclasses/WildComments.java", complianceLevel = 22)
	void testWildComments(@BySimpleName("WildComments") CtClass<?> type) {
		//contract: tests that value of comment is correct even for wild combinations of characters. See WildComments class for details
		List<CtLiteral<String>> literals = (List) ((CtNewArray<?>) type.getField("comments").getDefaultExpression()).getElements();
		assertTrue(literals.size() > 10);
		/*
		 * each string literal has a comment and string value, which defines expected value of its comment
		 */
		for (CtLiteral<String> literal : literals) {
			assertEquals(1, literal.getComments().size());
			CtComment comment = literal.getComments().get(0);
			String expected = literal.getValue();
			assertEquals(expected, comment.getContent(), literal.getPosition().toString());
		}
	}
}
