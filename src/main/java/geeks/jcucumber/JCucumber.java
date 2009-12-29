package geeks.jcucumber;

import java.net.URL;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;

import geeks.expressive.*;
import geeks.jcucumber.internal.Parser;

/**
 * A simplistic implementation of Cucumber.
 *
 * @author pabstec
 */
public class JCucumber {
  private final ResultPublisher resultPublisher;

  public JCucumber(ResultPublisher resultPublisher) {
    this.resultPublisher = resultPublisher;
  }

  public void run(URL featureResource, Scope stepsScope) throws IOException {
    run(new InputStreamReader(featureResource.openStream(), "UTF-8"), stepsScope);
  }

  public void run(Reader reader, Scope stepsScope) throws IOException {
    ObjectFactory parserObjectFactory = new DefaultObjectFactory();
    parserObjectFactory.addInstance(ResultPublisher.class, resultPublisher);
    parserObjectFactory.addInstance(Scope.class, stepsScope);
    Expressive parserExpressive = new Expressive(parserObjectFactory);
    parserExpressive.execute(new BufferedReader(reader), Parser.COMMAND_ASSOCIATION,
            MethodRegexAssociation.NONE, Scopes.asScope(Parser.class));
    parserObjectFactory.getInstance(Parser.class).finished();
  }

}
