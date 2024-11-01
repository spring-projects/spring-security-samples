<?php

$config = array(

    'admin' => array(
        'core:AdminPassword',
    ),

    'example-userpass' => array(
        'exampleauth:UserPass',
        'user1:user1pass' => array(
            'uid' => array('1'),
            'eduPersonAffiliation' => array('group1'),
            'email' => 'user1@example.org',
        ),
        'user2:user2pass' => array(
            'uid' => array('2'),
            'eduPersonAffiliation' => array('group2'),
            'email' => 'user2@example.org',
        ),
        'customer:password' => array(
            'uid' => array('3'),
            'eduPersonAffiliation' => array('group1'),
            'email' => 'customer@example.org',
            'firstName' => 'Customer',
            'lastName' => 'Example'
        ),

    ),

);
