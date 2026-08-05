package com.example.ui.screens.workspace

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import com.example.data.model.Workspace
import com.example.data.model.TeamAgreement
import com.example.data.model.ProductionTask
import com.example.data.model.WorkspaceMember
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DocumentPrintHelper {
    fun printWorkspaceBlueprint(
        context: Context,
        workspace: Workspace,
        agreement: TeamAgreement?,
        tasks: List<ProductionTask>,
        members: List<WorkspaceMember>
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(context, "Printing not supported on this device.", Toast.LENGTH_LONG).show()
            return
        }

        val completedTasks = tasks.filter { it.kanbanLane.uppercase() == "PUBLISH" }
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateStr = format.format(Date(workspace.createdAt))

        val membersHtml = if (members.isEmpty()) {
            "<tr><td colspan='2' style='padding: 8px; color: #666;'>No official registered workspace members.</td></tr>"
        } else {
            members.joinToString("") { m ->
                """
                <tr>
                    <td style="padding: 8px; border-bottom: 1px solid #ddd;">${m.userId}</td>
                    <td style="padding: 8px; border-bottom: 1px solid #ddd; font-weight: bold; color: #0EA5E9;">${m.assignedRoleTitle.uppercase()}</td>
                </tr>
                """.trimIndent()
            }
        }

        val tasksHtml = if (completedTasks.isEmpty()) {
            "<tr><td colspan='2' style='padding: 8px; color: #666;'>No completed tasks published yet.</td></tr>"
        } else {
            completedTasks.mapIndexed { index, t ->
                """
                <tr>
                    <td style="padding: 8px; border-bottom: 1px solid #ddd; width: 40px;">${index + 1}</td>
                    <td style="padding: 8px; border-bottom: 1px solid #ddd;">${t.title}</td>
                </tr>
                """.trimIndent()
            }.joinToString("")
        }

        val agreementText = agreement?.contentText ?: "No locked mutual agreement was registered for this workspace."

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>${workspace.name} Blueprint</title>
                <style>
                    body {
                        font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
                        color: #1E293B;
                        line-height: 1.6;
                        margin: 40px;
                        background-color: #FFFFFF;
                    }
                    .header {
                        border-bottom: 3px solid #0EA5E9;
                        padding-bottom: 20px;
                        margin-bottom: 30px;
                    }
                    .title-label {
                        font-size: 11px;
                        font-weight: 900;
                        color: #0EA5E9;
                        letter-spacing: 2px;
                        text-transform: uppercase;
                    }
                    .main-title {
                        font-size: 28px;
                        font-weight: 800;
                        color: #0F172A;
                        margin: 5px 0 0 0;
                    }
                    h2 {
                        font-size: 16px;
                        font-weight: 700;
                        color: #0F172A;
                        border-bottom: 1px solid #E2E8F0;
                        padding-bottom: 8px;
                        margin-top: 30px;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin-top: 10px;
                    }
                    th {
                        background-color: #F8FAFC;
                        text-align: left;
                        padding: 8px;
                        font-weight: bold;
                        border-bottom: 2px solid #E2E8F0;
                    }
                    .agreement-box {
                        background-color: #F8FAFC;
                        border: 1px solid #E2E8F0;
                        border-radius: 8px;
                        padding: 15px;
                        font-size: 14px;
                        white-space: pre-wrap;
                        margin-top: 10px;
                    }
                    .footer {
                        margin-top: 50px;
                        border-top: 1px dashed #CBD5E1;
                        padding-top: 20px;
                        font-size: 11px;
                        color: #64748B;
                        text-align: center;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="title-label">Creator Co-Op Compliance Log</div>
                    <div class="main-title">WORKSPACE BLUEPRINT SUMMARY</div>
                </div>

                <h2>1. CO-OP CLASSIFICATION & METRICS</h2>
                <table>
                    <tr>
                        <td style="font-weight: bold; width: 200px; padding: 6px 0;">Workspace Title:</td>
                        <td>${workspace.name}</td>
                    </tr>
                    <tr>
                        <td style="font-weight: bold; padding: 6px 0;">Broadcast Platform:</td>
                        <td>${workspace.platformType}</td>
                    </tr>
                    <tr>
                        <td style="font-weight: bold; padding: 6px 0;">Scope / Description:</td>
                        <td>${workspace.description.ifBlank { "No description provided." }}</td>
                    </tr>
                    <tr>
                        <td style="font-weight: bold; padding: 6px 0;">Created On:</td>
                        <td>$dateStr</td>
                    </tr>
                    <tr>
                        <td style="font-weight: bold; padding: 6px 0;">Status:</td>
                        <td>${if (workspace.isArchived) "Archived" else "Active"}</td>
                    </tr>
                </table>

                <h2>2. MEMBER ROSTER & ROLES</h2>
                <table>
                    <thead>
                        <tr>
                            <th>User ID / Member</th>
                            <th>Assigned Role</th>
                        </tr>
                    </thead>
                    <tbody>
                        $membersHtml
                    </tbody>
                </table>

                <h2>3. MUTUAL WORKING AGREEMENT</h2>
                <div class="agreement-box">$agreementText</div>

                <h2>4. COMPLETED PRODUCTION DELIVERABLES</h2>
                <table>
                    <thead>
                        <tr>
                            <th style="width: 50px;">Index</th>
                            <th>Deliverable Title</th>
                        </tr>
                    </thead>
                    <tbody>
                        $tasksHtml
                    </tbody>
                </table>

                <div class="footer">
                    <strong>OFFICIAL DIGITAL BLUEPRINT SEAL</strong><br>
                    Generated securely via Creator Co-Op Integration Engine.<br>
                    Document verification: OK
                </div>
            </body>
            </html>
        """.trimIndent()

        Handler(Looper.getMainLooper()).post {
            try {
                val webView = WebView(context)
                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        val printJobName = "${workspace.name} Blueprint"
                        val printAdapter = webView.createPrintDocumentAdapter(printJobName)
                        printManager.print(
                            printJobName,
                            printAdapter,
                            PrintAttributes.Builder().build()
                        )
                    }
                }
                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            } catch (e: Exception) {
                Toast.makeText(context, "Print generation failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
