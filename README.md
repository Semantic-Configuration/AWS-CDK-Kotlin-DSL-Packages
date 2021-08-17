AWS CDK Kotlin DSL Packages
===
This repository consists of three sources of Maven artifacts:

1. Latest artifacts published at
   https://github.com/orgs/Semantic-Configuration/packages?repo_name=AWS-CDK-Kotlin-DSL-Packages
2. Old artifacts originally published at https://github.com/orgs/justincase-jp/packages?repo_name=AWS-CDK-Kotlin-DSL
   1. Republished at https://github.com/Semantic-Configuration/AWS-CDK-Kotlin-DSL-Packages/tree/legacy/maven2
3. Old artifacts originally published at https://bintray.com/justincase/aws-cdk-kotlin-dsl
   1. Republished at https://github.com/Semantic-Configuration/AWS-CDK-Kotlin-DSL-Packages/tree/legacy/maven1

This branch holds the proxy implementation (using Cloudflare, Heroku, and JFrog Platform) that combines
the above three sources as `https://cdk.lemm.io/maven`, and at the same time, gets rid of
the [GitHub Packages Authorization restriction](
  https://github.community/t/how-to-allow-unauthorised-read-access-to-github-packages-maven-repository/115517
).
