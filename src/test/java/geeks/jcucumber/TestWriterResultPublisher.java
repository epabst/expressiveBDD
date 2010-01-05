package geeks.jcucumber;

import org.easymock.classextension.EasyMock;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import static org.easymock.classextension.EasyMock.*;

/**
 * A test for {@link WriterResultPublisher}.
 *
 * @author pabstec
 */
public class TestWriterResultPublisher {
  @Test
  public void shouldFlushEachLine() throws IOException {
    Writer writer = createMock(Writer.class);
    expectPrintln(writer, "The next step is coming");
    expectPrintln(writer, "PASSED:Step #1");
    replay(writer);

    WriterResultPublisher resultPublisher = new WriterResultPublisher(writer);
    resultPublisher.writeln("The next step is coming");
    resultPublisher.stepPassed("Step #1");
    verify(writer);
  }

  private void expectPrintln(Writer writer, String line) throws IOException {
    //this is how PrintWriter works
    writer.write(line, 0, line.length());
    expectLastCall().once();
    writer.write(System.getProperty("line.separator"));
    expectLastCall().once();
    writer.flush();
    expectLastCall().once();
  }
}
