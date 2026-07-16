# Quick Copy-Paste Prompt for AI

---

**Copy everything below this line and paste into your AI chat:**

---

I need you to fix my GitHub Actions workflow that's failing with a "No such file or directory" error.

**Problem:** The workflow expects the project in a subdirectory (like `service-name/`) but my repository has the project at the root level.

**Tasks:**

1. Find the workflow file in `.github/workflows/` directory
2. Make these changes:
   - Remove `paths:` filter if it references a subdirectory like `"service-name/**"`
   - Remove all `working-directory: service-name` lines from build steps
   - Update Docker build from `--file service-name/Dockerfile` to `--file Dockerfile`
   - Update Docker build context from `service-name` to `.` (current directory)
   - Update environment variables like `TASK_DEF_FILE: service-name/ecs-task-definition.json` to `TASK_DEF_FILE: ecs-task-definition.json`
   - Update cache paths from `service-name/.mvn/wrapper` to `.mvn/wrapper`
   - Update cache keys from `hashFiles('service-name/pom.xml')` to `hashFiles('pom.xml')`
   - Verify the Java/Node/Python version in the workflow matches what the project actually uses

3. Add `staging` branch to workflow triggers if not already present:
   ```yaml
   on:
     push:
       branches:
         - main
         - staging
   ```

4. Commit changes with message: "Fix GitHub Actions workflow for root-level project structure" and include this trailer:
   ```
   Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>
   ```

5. Push to the current branch

**Expected outcome:** Build and test steps should succeed. Deployment steps may fail if cloud credentials aren't configured (this is expected).

Please show me the changes you make and confirm when the workflow is fixed.

---

**End of prompt**
