package org.pac4j.saml.sso.impl;

import org.junit.Test;
import org.opensaml.saml.common.messaging.context.SAMLMetadataContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.pac4j.saml.context.SAML2MessageContext;
import org.pac4j.saml.crypto.SAML2SignatureTrustEngineProvider;
import org.pac4j.saml.exceptions.SAMLException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SAML2DefaultResponseValidatorTests {

  @Test
  public void testDoesNotWantAssertionsSignedWithNullContext() throws Exception {
    SAML2DefaultResponseValidator validator = createResponseValidatorWithSigningValidationOf(false);
    assertFalse("Expected wantAssertionsSigned == false", validator.wantsAssertionsSigned(null));
  }

  private SAML2DefaultResponseValidator createResponseValidatorWithSigningValidationOf(boolean wantsAssertionsSigned) {
    SAML2SignatureTrustEngineProvider trustEngineProvider = mock(SAML2SignatureTrustEngineProvider.class);
    Decrypter decrypter = mock(Decrypter.class);
    return new SAML2DefaultResponseValidator(trustEngineProvider, decrypter, 0, wantsAssertionsSigned);
  }

  @Test
  public void testWantsAssertionsSignedWithNullContext() throws Exception {
    SAML2DefaultResponseValidator validator = createResponseValidatorWithSigningValidationOf(true);
    assertTrue("Expected wantAssertionsSigned == true", validator.wantsAssertionsSigned(null));
  }

  @Test
  public void testDoesNotWantAssertionsSignedWithNullSPSSODescriptor() throws Exception {
    SAML2DefaultResponseValidator validator = createResponseValidatorWithSigningValidationOf(false);
    SAML2MessageContext context = new SAML2MessageContext();
    assertNull("Expected SPSSODescriptor to be null", context.getSPSSODescriptor());
    assertFalse("Expected wantAssertionsSigned == false", validator.wantsAssertionsSigned(context));
  }

  @Test
  public void testWantsAssertionsSignedWithNullSPSSODescriptor() throws Exception {
    SAML2DefaultResponseValidator validator = createResponseValidatorWithSigningValidationOf(true);
    SAML2MessageContext context = new SAML2MessageContext();
    assertNull("Expected SPSSODescriptor to be null", context.getSPSSODescriptor());
    assertTrue("Expected wantAssertionsSigned == true", validator.wantsAssertionsSigned(context));
  }

  @Test
  public void testDoesNotWantAssertionsSignedWithValidSPSSODescriptor() throws Exception {
    SAML2DefaultResponseValidator validator = createResponseValidatorWithSigningValidationOf(false);
    SAML2MessageContext context = new SAML2MessageContext();

    SAMLMetadataContext samlSelfMetadataContext = context.getSAMLSelfMetadataContext();
    SPSSODescriptor roleDescriptor = mock(SPSSODescriptor.class);
    when(roleDescriptor.getWantAssertionsSigned()).thenReturn(false);
    samlSelfMetadataContext.setRoleDescriptor(roleDescriptor);

    assertNotNull("Expected SPSSODescriptor to not be null", context.getSPSSODescriptor());
    assertFalse("Expected wantAssertionsSigned == false", validator.wantsAssertionsSigned(context));
  }

  @Test
  public void testWantsAssertionsSignedWithValidSPSSODescriptor() throws Exception {
    SAML2DefaultResponseValidator validator = createResponseValidatorWithSigningValidationOf(true);
    SAML2MessageContext context = new SAML2MessageContext();

    SAMLMetadataContext samlSelfMetadataContext = context.getSAMLSelfMetadataContext();
    SPSSODescriptor roleDescriptor = mock(SPSSODescriptor.class);
    when(roleDescriptor.getWantAssertionsSigned()).thenReturn(true);
    samlSelfMetadataContext.setRoleDescriptor(roleDescriptor);

    assertNotNull("Expected SPSSODescriptor to not be null", context.getSPSSODescriptor());
    assertTrue("Expected wantAssertionsSigned == true", validator.wantsAssertionsSigned(context));
  }

  @Test(expected = SAMLException.class)
  public void testAssertionWithoutSignatureThrowsException() throws Exception {
    SAML2DefaultResponseValidator validator = createResponseValidatorWithSigningValidationOf(true);
    SAML2MessageContext context = new SAML2MessageContext();
    SAMLPeerEntityContext peerEntityContext = new SAMLPeerEntityContext();
    peerEntityContext.setAuthenticated(false);
    context.addSubcontext(peerEntityContext);
    validator.validateAssertionSignature(null, context, null);
  }

  @Test
  public void testAssertionWithoutSignatureDoesNotThrowException() throws Exception {
    SAML2DefaultResponseValidator validator = createResponseValidatorWithSigningValidationOf(false);
    SAML2MessageContext context = new SAML2MessageContext();
    SAMLPeerEntityContext peerEntityContext = new SAMLPeerEntityContext();
    peerEntityContext.setAuthenticated(false);
    context.addSubcontext(peerEntityContext);
    validator.validateAssertionSignature(null, context, null);
    // expected no exceptions
  }
}