package jetbrains.buildServer.commandline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.impl.personal.PersonalPatchUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.vcs.*;

public class MappingGenerator {
  private final VcsManager myVcsManager;
  private final SBuildType myBuildType;
  private List<MappingElement> myLines;

  private VcsRootEntry myCurrentEntry;

  public MappingGenerator(final VcsManager vcsManager, final SBuildType buildType) {
    myVcsManager = vcsManager;
    myBuildType = buildType;
  }

  public List<MappingElement> getLines() {
    return myLines;
  }

  public void generateVcsMapping() {
    myLines = new ArrayList<MappingElement>();

    for (VcsRootEntry entry : myBuildType.getVcsRootEntries()) {
      myCurrentEntry = entry;
      generateMappingForEntry();
    }
  }

  private void generateMappingForEntry() {
    try {
      final VcsPersonalSupport personalSupport = getPersonalSupport();

      if (personalSupport != null) {
        obtainMappingUsing(personalSupport);
      }
    } catch (VcsException e) {
      Loggers.SERVER.warn(e);
      // TODO
    }
  }

  private VcsPersonalSupport getPersonalSupport() {
    final String vcsName = (myCurrentEntry.getVcsRoot()).getVcsName();
    return myVcsManager.findVcsPersonalSupportByName(vcsName);
  }

  private void obtainMappingUsing(final VcsPersonalSupport personalSupport) throws VcsException {
    if (personalSupport instanceof IncludeRuleBasedMappingProvider) {
      IncludeRuleBasedMappingProvider mappingProvider = (IncludeRuleBasedMappingProvider)personalSupport;

      buildMappingForIncludeRules(mappingProvider);
    }
  }

  private void buildMappingForIncludeRules(final IncludeRuleBasedMappingProvider mappingProvider) throws VcsException {
    final SVcsRoot vcsRoot = (SVcsRoot)myCurrentEntry.getVcsRoot();

    for (IncludeRule includeRule : myCurrentEntry.getCheckoutRules().getIncludeRules()) {

      final Collection<VcsClientMapping> pathPrefixes = mappingProvider.getClientMapping(vcsRoot, includeRule);

      for (VcsClientMapping info2TargetPath : pathPrefixes) {

        final String leftPart = createLeftPart(info2TargetPath);
        final String rightPart = vcsRoot.getVcsName() + PersonalPatchUtil.SEPARATOR +
                                 StringUtil.removeTailingSlash(info2TargetPath.getVcsUrlInfo());
        myLines.add(new MappingElement(leftPart, rightPart, makeDescription(vcsRoot, includeRule)));
      }

    }
  }

  private String createLeftPart(final VcsClientMapping info2TargetPath) {
    String result = StringUtil.removeTailingSlash(info2TargetPath.getTargetPath());
    return "".equals(result) ? "." : result;
  }

  private static String makeDescription(final SVcsRoot vcsRoot, final IncludeRule includeRule) {
    if ("".equals(includeRule.getFrom()) && "".equals(includeRule.getTo())) {
      return vcsRoot.getDescription();
    }
    return vcsRoot.getDescription() + "; " + includeRule.toDescriptiveString();
  }

}
