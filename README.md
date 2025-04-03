# vault-approle-auth
Vault AppRole Authorization Example

## Назначение программы
Это пример интеграции с Vault на Java по AppRole с использованием
библиотеки 'io.github.jopenlibs:vault-java-driver:6.2.0'

Программа
- получает wrapped token сгенерированный из role_id, secret_id
- обменивает (un-wrap) токен на role_id, secret_id
- делает login по role_id, secret_id и получает сессионный токен
- читает секрет из KV хранилища Vault используя сессионный токен 

## Тестирование интеграции с Vault в локальном режиме

Запустить сервер в dev моде
```
vault server -dev
```

Подготовить запуск  CLI
```
export VAULT_ADDR="http://127.0.0.1:8200"
export VAULT_TOKEH=<root token>
```

В другом терминале создать роль 

```vault_init.sh
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
```

Ниже выхлоп после создания роли
```
$ ./vault_init.sh | tee out.txt
Success! Enabled approle auth method at: approle/
Success! Data written to: auth/approle/role/my-role
Key        Value
---        -----
role_id    0573b8f2-89c3-7976-adcb-30b3fbe9faea
Key                   Value
---                   -----
secret_id             9da1065c-ed2a-3109-9fe9-83a1298f154d
secret_id_accessor    24853eef-853c-5837-01bd-18fc8151512f
secret_id_num_uses    40
secret_id_ttl         24h```

Из этого выхлопа делаем json  при помощи awk скрипта json.awk
```awk
BEGIN { print "{" }
/^role_id / || /^secret_id / { print "\"" $1 "\"" ":" "\"" $2 "\""}
END { print "}"}
```

```
$ awk -f json.awk out.txt
{
"role_id":"0573b8f2-89c3-7976-adcb-30b3fbe9faea"
"secret_id":"9da1065c-ed2a-3109-9fe9-83a1298f154d"
}
```

Делаем wrap на полученный json
- заходим на UI Vault http://127.0.0.1:8200/ui/vault/dashboard
- далее Tools -> Wrap. Вставляем json и жмем кнопку Wrpa. Полученный wrapped token сохраняем в переменной wrappedToken

Далее на Vault UI добавляем секреты "Secret Engines" -> "key/value secret storage" -> "Create secret" по пути /my-role/jdbc с ключом password

Путь к секрету для конфигурирования - secret/my-role/jdbc/password

Мы готовы к тестированию







