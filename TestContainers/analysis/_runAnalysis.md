The files with names:

debthOfAbstraction.md
metalinguisticAbstraction.md
sideEffects.md
nplusOneQueries.md
readAfterUpdatePattern.md
missingAtomicityOfTheCommands.md
fullObjectUpdate.md
missingProjectionAndUnnecessaryDataLoad.md
dataTransferObjectUsedForManyThings.md
unnecessaryInterfaces.md
numberOfDependenciesInjectedInClasses.md

Contain commont antipatterns.

Create a plan on how to analyze the codebase in the context of these files and identify the occurances of the antipatterns for each file.

Action the plan to generate json file with the following format


{
  "version": "2.1.0",
  "$schema": "http://json.schemastore.org/sarif-2.1.0-rtm.5",
  "runs": [
    {
      "tool": {
        "driver": {
          "name": "AI Linter",
          "rules": [
            {
              "id": "data transfer objects used for many things",
              "shortDescription": {
                "text": "data transfer objects used for many things"
              }
            },
            {
              "id": "debth of abstraction",
              "shortDescription": {
                "text": "debth of abstraction"
              }
            },
            {
              "id": "full object update",
              "shortDescription": {
                "text": "full object update"
              }
            },
            {
              "id": "metalinguistic abstraction",
              "shortDescription": {
                "text": "metalinguistic abstraction"
              }
            },
            {
              "id": "missing atomicity",
              "shortDescription": {
                "text": "missing atomicity"
              }
            },
            {
              "id": "missing projection",
              "shortDescription": {
                "text": "missing projection"
              }
            },
            {
              "id": "N+1 query execution",
              "shortDescription": {
                "text": "N+1 query execution"
              }
            },
            {
              "id": "number of dependencies",
              "shortDescription": {
                "text": "number of dependencies"
              }
            },
            {
              "id": "read after update",
              "shortDescription": {
                "text": "read after update"
              }
            },
            {
              "id": "side effects",
              "shortDescription": {
                "text": "side effects"
              }
            },
            {
              "id": "streaming",
              "shortDescription": {
                "text": "streaming"
              }
            },
            {
              "id": "unnecessary interfaces",
              "shortDescription": {
                "text": "unnecessary interfaces"
              }
            }
          ]
        }
      },
      "results": []
    }
}

Every finding is a warning by itself. The format is foolowing and it is appended on the report in the array of results:
{
    "level": "error",
    "message": {
    "text": "Issue with flow"
    },
    "locations": [
    {
        "physicalLocation": {
            "artifactLocation": {
                "uri": "src/File1.xoo"
            },
            "region": {
                "startLine": 1,
                "startColumn": 5,
                "endLine": 1,
                "endColumn": 9
            }
        }
    }]
}

