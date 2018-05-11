#!/bin/sh

openssl aes-256-cbc -K $encrypted_2129fb3f7525_key -iv $encrypted_2129fb3f7525_iv -in travis-deploy-key.enc -out travis-deploy-key -d;
chmod 600 travis-deploy-key;
cp travis-deploy-key ~/.ssh/id_rsa;