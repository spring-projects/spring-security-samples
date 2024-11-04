<?php
$port = getenv("PORT");

// Spring Security SP

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

// Spring Security SAML SP

$metadata["http://localhost:$port/saml/metadata"] = array(
    'AssertionConsumerService' => "http://localhost:$port/saml/SSO",
    'SingleLogoutService' => [
        [
            'Binding' => 'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect',
            'Location' => "http://localhost:$port/saml/logout",
            'ResponseLocation' => "http://localhost:$port/saml/SingleLogout"
        ]
    ],
    'NameIDFormat' => 'urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress',
    'simplesaml.nameidattribute' => 'emailAddress',
    'assertion.encryption' => FALSE,
    'nameid.encryption' => FALSE,
    'validate.authnrequest' => FALSE,
    'redirect.sign' => TRUE,
);
?>
