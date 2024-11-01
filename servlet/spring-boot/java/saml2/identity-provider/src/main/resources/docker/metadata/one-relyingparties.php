<?php
$port = getenv("PORT");
$metadata["http://localhost:$port/saml2/metadata"] = array(
    'AssertionConsumerService' => "https://localhost:$port/login/saml2/sso",
    'SingleLogoutService' => "https://localhost:$port/logout/saml2/slo",
    'NameIDFormat' => 'urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress',
    'simplesaml.nameidattribute' => 'emailAddress',
    'assertion.encryption' => FALSE,
    'nameid.encryption' => FALSE,
    'validate.authnrequest' => FALSE,
    'redirect.sign' => TRUE,
);
?>
