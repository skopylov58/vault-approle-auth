BEGIN { print "{" }
/^role_id / || /^secret_id / { print "\"" $1 "\"" ":" "\"" $2 "\""}
END { print "}"}