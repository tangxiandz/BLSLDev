using BLSLDev_api.Data;
using Microsoft.EntityFrameworkCore;

// 设置EPPlus 8许可证（必须在创建ExcelPackage之前设置）
OfficeOpenXml.ExcelPackage.License.SetNonCommercialPersonal("BLSL");

var builder = WebApplication.CreateBuilder(args);

// 添加CORS配置
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll",
        builder =>
        {
            builder.AllowAnyOrigin()
                   .AllowAnyMethod()
                   .AllowAnyHeader();
        });
});

// 添加数据库连接 - 从配置文件读取连接字符串
builder.Services.AddDbContext<BLSLDbContext>(options =>
{
    var connectionString = builder.Configuration.GetConnectionString("DefaultConnection");
    options.UseSqlServer(connectionString);
});

// 添加控制器
builder.Services.AddControllers()
    .AddJsonOptions(options =>
    {
        options.JsonSerializerOptions.PropertyNamingPolicy = null; // 使用帕斯卡命名法
    });

// 添加Swagger
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

// 配置CORS - 必须在UseHttpsRedirection之前
app.UseCors("AllowAll");

// 初始化数据库
using (var scope = app.Services.CreateScope())
{
    var dbContext = scope.ServiceProvider.GetRequiredService<BLSLDbContext>();
    dbContext.Database.EnsureCreated();
}

// 配置Swagger
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();

app.Run();