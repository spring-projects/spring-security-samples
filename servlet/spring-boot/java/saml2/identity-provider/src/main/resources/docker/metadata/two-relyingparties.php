<?php
$port = getenv("PORT");
$metadata["http://localhost:$port/saml2/metadata"] = array(
    'AssertionConsumerService' => "http://localhost:$port/login/saml2/sso",
    'SingleLogoutService' => "http://localhost:$port/logout/saml2/slo",
    'NameIDFormat' => 'urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress',
    'simplesaml.nameidattribute' => 'emailAddress',
    'assertion.encryption' => FALSE,
    'nameid.encryption' => FALSE,
    'validate.authnrequest' => FALSE,
    'redirect.sign' => TRUE,
);
?>
