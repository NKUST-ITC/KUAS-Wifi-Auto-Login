Imports System.Text
Imports System.Net.Security
Imports System.Security.Cryptography.X509Certificates
Imports System.Net
Imports System.IO
Imports KUAS_WiFi.SilentWebModule
Imports System.Text.RegularExpressions

Public Class LoginFrm

    Dim IANGONG_WIFI_SERVER As String = "172.16.61.253"
    Dim YANCHAO_WIFI_SERVER As String = "172.16.109.253"

    Public Declare Function SendMessage Lib "user32" Alias "SendMessageA" _
                            (ByVal hwnd As IntPtr,
                             ByVal wMsg As Integer,
                             ByVal wParam As IntPtr,
                             ByVal lParam As Byte()) _
                             As Integer
    Public Const EM_SETCUEBANNER As Integer = &H1501

    Private Sub LoginButton_Click(sender As Object, e As EventArgs) Handles LoginButton.Click
        disableViews()

        Dim response As HttpWebResponse = HttpWebResponseUtility.CreateGetHttpResponse("http://172.16.109.253", 7000, Nothing, Nothing)
        Dim reader As StreamReader = New StreamReader(response.GetResponseStream, System.Text.Encoding.GetEncoding("UTF-8"))
        Dim respHTML As String = reader.ReadToEnd()
        checkLoginLocation(response.Headers.[Get]("Location"))
        response.Close()
    End Sub

    Private Sub disableViews()
        User.Enabled = False
        Pwd.Enabled = False
        LoginButton.Enabled = False
        LogoutButton.Enabled = False
    End Sub

    Private Sub enableViews()
        User.Enabled = True
        Pwd.Enabled = True
        LoginButton.Enabled = True
        LogoutButton.Enabled = True
    End Sub

    Private Sub checkLoginLocation(_location As String)
        Dim regex As Regex = New Regex("\d+\.\d+\.\d+\.\d+")
        Dim match As Match = regex.Match(_location)
        If (_location.Length = 0) Then
            MsgBox("請求 Wi-Fi 伺服器的連線逾時。", MsgBoxStyle.Critical)
            enableViews()
        Else
            If (_location.Contains("auth_entry")) Then
                If match.Success Then
                    login(match.Value)
                Else
                    MsgBox("發生錯誤！", MsgBoxStyle.Critical)
                    enableViews()
                End If
            Else
                If (_location.Contains("login_online") Or _location.Contains("login.php")) Then
                    MsgBox("您已經登入或是有可用網路了。", MsgBoxStyle.Information)
                    enableViews()
                End If
            End If
        End If
    End Sub

    Private Sub login(_host As String)
        Dim parameters As IDictionary(Of String, String) = New Dictionary(Of String, String)()
        parameters.Add("username", System.Uri.EscapeDataString(User.Text))
        parameters.Add("userpwd", System.Uri.EscapeDataString(Pwd.Text))
        parameters.Add("login", "")
        parameters.Add("orig_referer", "")

        Dim response As HttpWebResponse = HttpWebResponseUtility.CreatePostHttpResponse("http://" + _host + "/cgi-bin/ace_web_auth.cgi", parameters, 7000, Nothing, Encoding.UTF8, Nothing)
        Dim reader As StreamReader = New StreamReader(response.GetResponseStream, System.Text.Encoding.GetEncoding("UTF-8"))
        Dim respHTML As String = reader.ReadToEnd()

        If (respHTML.Contains("reason=")) Then
            Dim _reason As String = respHTML.Substring(respHTML.IndexOf("reason=") + 7, respHTML.IndexOf("&", respHTML.IndexOf("reason=") + 7) - respHTML.IndexOf("reason=") - 7)
            MsgBox(dumpReason(_reason), MsgBoxStyle.Critical)
        ElseIf (respHTML.Contains("404 - Not Found")) Then
            MsgBox("請求 Wi-Fi 伺服器的連線逾時。", MsgBoxStyle.Critical)
        ElseIf (respHTML.Contains("login_online_detail.php")) Then
            If (_host.Equals(IANGONG_WIFI_SERVER)) Then
                MsgBox("Wi-Fi登入成功, 歡迎來到高應大建工校區！", MsgBoxStyle.Information)
            Else
                MsgBox("Wi-Fi登入成功, 歡迎來到高應大燕巢校區！", MsgBoxStyle.Information)
            End If
        End If
        enableViews()
    End Sub

    Private Sub LoginFrm_Load(sender As Object, e As EventArgs) Handles Me.Load
        SendMessage(User.Handle,
                            EM_SETCUEBANNER,
                            IntPtr.Zero,
                            System.Text.Encoding.Unicode.GetBytes("學號"))
        SendMessage(Pwd.Handle,
                             EM_SETCUEBANNER,
                             IntPtr.Zero,
                             System.Text.Encoding.Unicode.GetBytes("密碼"))
    End Sub
End Class

Namespace SilentWebModule
    ''' <summary>
    ''' 有關HTTP請求的模組
    ''' </summary>
    Public Class HttpWebResponseUtility
        Private Shared ReadOnly DefaultUserAgent As String = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0"
        ''' <summary>
        ''' 創建GET方式的HTTP請求
        ''' </summary>
        ''' <param name="url">請求的URL</param>
        ''' <param name="timeout">請求的超時時間</param>
        ''' <param name="userAgent">請求的客戶端瀏覽器信息，可以為空</param>
        ''' <param name="cookies">隨同HTTP請求發送的Cookie信息，如果不需要身分驗證可以為空</param>
        ''' <returns></returns>
        Public Shared Function CreateGetHttpResponse(url As String, timeout As System.Nullable(Of Integer), userAgent As String, cookies As CookieContainer) As HttpWebResponse
            If String.IsNullOrEmpty(url) Then
                Throw New ArgumentNullException("url")
            End If
            Dim request As HttpWebRequest = TryCast(WebRequest.Create(url), HttpWebRequest)
            request.Method = "GET"
            request.KeepAlive = True
            request.UserAgent = DefaultUserAgent
            request.AllowAutoRedirect = False

            If Not String.IsNullOrEmpty(userAgent) Then
                request.UserAgent = userAgent
            End If
            If timeout.HasValue Then
                request.Timeout = timeout.Value
            End If

            If cookies IsNot Nothing Then
                request.CookieContainer = cookies
            End If
            Return TryCast(request.GetResponse(), HttpWebResponse)
        End Function
        ''' <summary>
        ''' 創建POST方式的HTTP請求
        ''' </summary>
        ''' <param name="url">請求的URL</param>
        ''' <param name="parameters">隨同請求POST的參數名稱及參數值字典</param>
        ''' <param name="timeout">請求的超時時間</param>
        ''' <param name="userAgent">請求的客戶端瀏覽器信息，可以為空</param>
        ''' <param name="requestEncoding">發送HTTP請求時所用的編碼</param>
        ''' <param name="cookies">隨同HTTP請求發送的Cookie信息，如果不需要身分驗證可以為空</param>
        ''' <returns></returns>
        Public Shared Function CreatePostHttpResponse(url As String, parameters As IDictionary(Of String, String), timeout As System.Nullable(Of Integer), userAgent As String, requestEncoding As Encoding, cookies As CookieContainer) As HttpWebResponse
            System.Net.ServicePointManager.Expect100Continue = False '防止417

            If String.IsNullOrEmpty(url) Then
                Throw New ArgumentNullException("url")
            End If
            If requestEncoding Is Nothing Then
                Throw New ArgumentNullException("requestEncoding")
            End If
            Dim request As HttpWebRequest = Nothing
            '如果是發送HTTPS請求
            If url.StartsWith("https", StringComparison.OrdinalIgnoreCase) Then
                ServicePointManager.ServerCertificateValidationCallback = New RemoteCertificateValidationCallback(AddressOf CheckValidationResult)
                request = TryCast(WebRequest.Create(url), HttpWebRequest)
                request.ProtocolVersion = HttpVersion.Version11
            Else
                request = TryCast(WebRequest.Create(url), HttpWebRequest)
            End If
            request.Method = "POST"
            request.KeepAlive = True
            request.ContentType = "application/x-www-form-urlencoded"
            request.AllowAutoRedirect = False

            If Not String.IsNullOrEmpty(userAgent) Then
                request.UserAgent = userAgent
            Else
                request.UserAgent = DefaultUserAgent
            End If

            If timeout.HasValue Then
                request.Timeout = timeout.Value
            End If
            If cookies IsNot Nothing Then
                request.CookieContainer = cookies
            End If
            '如果需要POST數據
            If Not (parameters Is Nothing OrElse parameters.Count = 0) Then
                Dim buffer As New StringBuilder()
                Dim i As Integer = 0
                For Each key As String In parameters.Keys
                    If i > 0 Then
                        buffer.AppendFormat("&{0}={1}", key, parameters(key))
                    Else
                        buffer.AppendFormat("{0}={1}", key, parameters(key))
                    End If
                    i += 1
                Next
                Dim data As Byte() = requestEncoding.GetBytes(buffer.ToString())
                Using stream As Stream = request.GetRequestStream()
                    stream.Write(data, 0, data.Length)
                End Using
            End If
            Return TryCast(request.GetResponse(), HttpWebResponse)

        End Function

        Private Shared Function CheckValidationResult(sender As Object, certificate As X509Certificate, chain As X509Chain, errors As SslPolicyErrors) As Boolean
            Return True
            '總是接受
        End Function
    End Class
End Namespace
