#!/usr/bin/bash

#enable app role
vault auth enable approle

#create role my-role
#./vault write auth/approle/role/my-role token_type=batch secret_id_ttl=10m token_ttl=20m token_max_ttl=30m secret_id_num_uses=40
vault write auth/approle/role/my-role token_type=batch secret_id_ttl=1d token_ttl=1d token_max_ttl=1d secret_id_num_uses=40

#read role_id
vault read auth/approle/role/my-role/role-id

#read secret_id
vault write -f auth/approle/role/my-role/secret-id



