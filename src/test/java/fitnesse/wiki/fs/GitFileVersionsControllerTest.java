package fitnesse.wiki.fs;

import fitnesse.wiki.WikiPage;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled
class GitFileVersionsControllerTest {

  private VersionsControllerFixture fixture;

  @BeforeAll
  public void setUp() throws GitAPIException, IOException {
    fixture = new VersionsControllerFixture(GitFileVersionsController.class.getCanonicalName());
    fixture.cleanUp();
    fixture.createWikiRoot();
    new GitVersionsControllerFixture().initialiseGitRepository();
  }

  @AfterAll
  public void tearDown() throws IOException {
    fixture.cleanUp();
  }

  @Test
  void shouldAddFileToVersionControl() {
    fixture.savePageWithContent("TestPage", "content");
  }

  @Test
  void shouldDeleteFileFromVersionControl() {
    fixture.savePageWithContent("TestPage", "content");
    fixture.deletePage("TestPage");
  }

  @Test
  void shouldReadRecentChangesOnEmptyRepository() {
    GitFileVersionsController versionsController = new GitFileVersionsController();
    WikiPage recentChanges = versionsController.toWikiPage(fixture.getRootPage());
    assertTrue(recentChanges.getData().getContent().startsWith("Unable to read history: "), recentChanges.getData().getContent());
  }

  @Test
  void shouldReadRecentChanges() {
	// make sure the content is different otherwise GIT will not save any change and nothing is in the history  
	fixture.savePageWithContent("TestPage", "content");
    fixture.savePageWithContent("TestPage2", "more content");
    fixture.savePageWithContent("TestPage", "different content");

    GitFileVersionsController versionsController = new GitFileVersionsController();
    WikiPage recentChanges = versionsController.toWikiPage(fixture.getRootPage());
    String expected = "|[FitNesse] Updated files: TestDir/RooT/TestPage.wiki.|".replace("/", File.separator);
    assertTrue(recentChanges.getData().getContent().startsWith(expected), String.format("Expected:\n%s\nActual:\n%s", expected, recentChanges.getData().getContent()));
  }

  @Test
  void shouldFormatSingleFileNameForCommit() {
    GitFileVersionsController versionsController = new GitFileVersionsController();
    String formatted = versionsController.formatFiles(new File[] {new File("simple.txt")});
    assertEquals("simple.txt", formatted);
  }

  @Test
  void shouldFormatFileNamesForCommit() {
    GitFileVersionsController versionsController = new GitFileVersionsController();
    String formatted = versionsController.formatFiles(new File[] {new File("simple.txt"), new File("middle.xml"), new File("complex/name.txt")});
    String expected = "simple.txt, middle.xml and complex/name.txt";
    expected = expected.replace("/", File.separator);
    assertEquals(expected , formatted);
  }

}
