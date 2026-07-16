# GitHub Actions Workflow Fix Prompt

Use this prompt to fix GitHub Actions workflows that fail due to incorrect directory structure assumptions.

---

## Prompt for AI

```
I need you to fix my GitHub Actions workflow. The workflow is failing with this error:

"An error occurred trying to start process '/usr/bin/bash' with working directory '/home/runner/work/{repo-name}/{repo-name}/{nested-dir}'. No such file or directory"

### Problem

My GitHub Actions workflow was configured for a mono-repo structure where the project lives in a subdirectory (e.g., `service-name/`), but my actual repository has the project at the root level. This causes the workflow to look for files in the wrong location.

### Tasks

1. **Analyze the current workflow file** at `.github/workflows/*.yml`
   - Identify all references to subdirectory paths
   - Check for `working-directory` parameters
   - Look for path filters in triggers

2. **Check the actual repository structure**
   - Verify where `pom.xml` (or equivalent build file) is located
   - Confirm the project is at root level, not in a subdirectory

3. **Fix the workflow** by updating these elements:

   **A. Remove or update the paths filter:**
   ```yaml
   # REMOVE THIS (if project is at root):
   on:
     push:
       paths:
         - "service-name/**"
   
   # REPLACE WITH:
   on:
     push:
       branches:
         - main
         - staging  # add staging if you want CI on staging branch
   ```

   **B. Remove working-directory from build steps:**
   ```yaml
   # REMOVE working-directory lines like:
   - name: Run tests
     working-directory: service-name
     run: mvn test
   
   # REPLACE WITH:
   - name: Run tests
     run: mvn test
   ```

   **C. Update Docker build paths:**
   ```yaml
   # CHANGE FROM:
   docker build \
     --file service-name/Dockerfile \
     --tag $IMAGE_URI:$IMAGE_TAG \
     service-name
   
   # TO:
   docker build \
     --file Dockerfile \
     --tag $IMAGE_URI:$IMAGE_TAG \
     .
   ```

   **D. Update file references in environment variables:**
   ```yaml
   # CHANGE FROM:
   TASK_DEF_FILE: service-name/ecs-task-definition.json
   
   # TO:
   TASK_DEF_FILE: ecs-task-definition.json
   ```

   **E. Update cache paths:**
   ```yaml
   # CHANGE FROM:
   path: |
     ~/.m2/repository
     service-name/.mvn/wrapper
   key: ${{ runner.os }}-maven-${{ hashFiles('service-name/pom.xml') }}
   
   # TO:
   path: |
     ~/.m2/repository
     .mvn/wrapper
   key: ${{ runner.os }}-maven-${{ hashFiles('pom.xml') }}
   ```

4. **Verify Java/Node version matches the project**
   - Check the actual Java/Node version used in the project
   - Update the workflow if it's using the wrong version
   - Example: If project uses Java 21 but workflow has Java 17, fix it

5. **Commit and push the changes**
   - Create a clear commit message explaining the fix
   - Push to the current branch
   - Include Co-authored-by trailer:
     ```
     Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>
     ```

### Expected Result

After the fix:
- ✅ Checkout code - should succeed
- ✅ Setup language runtime - should succeed
- ✅ Run tests - should succeed
- ✅ Build artifact (JAR/Docker image) - should succeed
- ❌ AWS/Cloud deployment steps - may fail if credentials not configured (this is expected)

### Additional Notes

- If the workflow still fails after path fixes, check for other issues:
  - Missing dependencies in the build file
  - Test failures (actual code issues)
  - Missing environment variables or secrets
  - Incorrect runtime versions

- The build should succeed even if deployment fails due to missing cloud credentials

### Output Format

Please:
1. Show me the specific changes you're making to the workflow file
2. Commit the changes with a descriptive message
3. Push to the current branch
4. Summarize what was fixed and what the build status is
```

---

## Example Before/After

### Before (Broken - expects mono-repo structure)
```yaml
on:
  push:
    branches:
      - main
    paths:
      - "service-name/**"

env:
  TASK_DEF_FILE: service-name/ecs-task-definition.json

jobs:
  build:
    steps:
      - name: Run tests
        working-directory: service-name
        run: mvn test
      
      - name: Build
        working-directory: service-name
        run: mvn package
      
      - name: Build Docker
        run: |
          docker build \
            --file service-name/Dockerfile \
            --tag $IMAGE:$TAG \
            service-name
```

### After (Fixed - works with root-level structure)
```yaml
on:
  push:
    branches:
      - main
      - staging

env:
  TASK_DEF_FILE: ecs-task-definition.json

jobs:
  build:
    steps:
      - name: Run tests
        run: mvn test
      
      - name: Build
        run: mvn package
      
      - name: Build Docker
        run: |
          docker build \
            --file Dockerfile \
            --tag $IMAGE:$TAG \
            .
```

---

## Quick Checklist

Use this checklist to verify all fixes:

- [ ] Removed or updated `paths` filter in workflow triggers
- [ ] Removed all `working-directory: service-name` lines
- [ ] Updated Dockerfile path from `service-name/Dockerfile` to `Dockerfile`
- [ ] Updated Docker build context from `service-name` to `.`
- [ ] Updated task definition path (if applicable)
- [ ] Updated cache paths to remove service name prefix
- [ ] Verified language version matches project (Java/Node/Python/etc)
- [ ] Committed changes with descriptive message
- [ ] Pushed to branch
- [ ] Verified workflow runs without "No such file or directory" error

---

## Troubleshooting

If you still see errors after applying these fixes:

1. **"No such file or directory"** - Still references to nested paths somewhere
   - Search the workflow for the service name as a path component
   - Check environment variables

2. **"mvn/npm/gradle not found"** - Build tool not in PATH
   - Ensure setup step for language is included
   - Verify the language version is correct

3. **"Tests failed"** - Actual test failures (not path issues)
   - This is expected if tests are broken
   - Run tests locally to debug

4. **"File not found: Dockerfile"** - Dockerfile might be in wrong location
   - Verify Dockerfile exists at repository root
   - Check if it's actually in a subdirectory

---

## Notes

- This fix applies to monorepo-style workflows being used in single-service repositories
- If you actually have a monorepo, you may need to keep the paths but adjust them differently
- The goal is to make paths in the workflow match the actual repository structure
