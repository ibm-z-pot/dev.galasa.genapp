package dev.galasa.genapp.postcode;

import org.apache.commons.logging.Log;
import org.assertj.core.api.Assertions;

import dev.galasa.core.manager.*;
import dev.galasa.Test;
import dev.galasa.BeforeClass;
import dev.galasa.AfterClass;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;

import dev.galasa.zos3270.Zos3270Terminal;
import dev.galasa.zos3270.ITerminal;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TextNotFoundException;
import dev.galasa.zos3270.ErrorTextFoundException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.Zos3270Exception;
import dev.galasa.zos3270.spi.NetworkException;

import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicsTerminal;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.CeciException;
import dev.galasa.cicsts.CeciManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.ICeciResponse;

@Test
public class TestPostcode {

   @Logger
   public Log logger;

   @CoreManager
   public ICoreManager coreManager;

   // @Zos3270Terminal(imageTag = "MOP")
   // public ITerminal terminal;
   public String imageTag = "MOP";

   @CicsRegion(cicsTag = "A")
   public ICicsRegion cics;

   @CicsTerminal(cicsTag = "A")
   public ICicsTerminal terminal;

   // Before executing tests - Logon to CICS
   @BeforeClass
   public void cicsLogon() throws CoreManagerException,
         TimeoutException,
         KeyboardLockedException,
         TerminalInterruptedException,
         TextNotFoundException,
         ErrorTextFoundException,
         NetworkException,
         FieldNotFoundException,
         Zos3270Exception {
      CredentialsUsernamePassword credential = (CredentialsUsernamePassword) coreManager.getCredentials(imageTag);
      coreManager.registerConfidentialText(credential.getPassword(), "user password");
      terminal.positionCursorToFieldContaining("Userid").tab().type(credential.getUsername())
            .positionCursorToFieldContaining("Password").tab().type(credential.getPassword()).enter()
            .waitForKeyboard();
      Assertions.assertThat(terminal.retrieveScreen()).containsOnlyOnce("Sign-on is complete");
   }

   // After executing Tests - Logoff from CICS
   @AfterClass
   public void cicsLogoff() throws TimeoutException,
         KeyboardLockedException,
         TerminalInterruptedException,
         TextNotFoundException,
         ErrorTextFoundException,
         NetworkException,
         FieldNotFoundException,
         Zos3270Exception {
      terminal.clear().waitForKeyboard()
            .type("CESF LOGOFF").enter().waitForKeyboard();
   }

   // Tests using CECI - PUT CONTAINER / LINK PROGRAM / GET CONTAINER
   @Test
   public void testLink() throws CeciException,
         CicstsManagerException,
         TimeoutException,
         KeyboardLockedException,
         TerminalInterruptedException,
         NetworkException,
         FieldNotFoundException {

      String programName = "LGACJV02";
      String channelName = "COBOL2jAVA";
      String inputContainerName = "LGACJV02-INPUT";
      String outputContainerName = "LGACJV02-OUTPUT";
      String inputData = "EU99 XYZ";

      logger.info("This is a galasa test of CECI LINK to " + programName);

      cics.ceci().startCECISession(terminal);

      ICeciResponse resp = cics.ceci().putContainer(terminal, channelName, inputContainerName, inputData, null, null,
            null);
      resp.checkNormal();

      // resp = cics.ceci().linkProgramWithChannel(ceciTerminal, programName,
      // channelName, null, null, false);
      // resp.checkNormal();

      // resp = cics.ceci().getContainer(ceciTerminal, channelName, containerName,
      // variableName, null, null);
      // resp.checkNormal();
      // assertThat(cics.ceci().retrieveVariableText(ceciTerminal, "&" +
      // variableName)).isUpperCase();
      // assertThat(cics.ceci().retrieveVariableText(ceciTerminal, "&" +
      // variableName)).startsWith(content.toUpperCase());
      terminal.pf3().waitForKeyboard();
   }

}
