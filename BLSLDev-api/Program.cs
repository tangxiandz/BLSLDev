using BLSLDev_api.Data;
using Microsoft.EntityFrameworkCore;

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

// 添加数据库连接
builder.Services.AddDbContext<BLSLDbContext>(options =>
{
    options.UseSqlServer("Server=118.31.79.35;Database=BLSL;User Id=sa;Password=Kpjzfr2r;TrustServerCertificate=true");
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

// 配置CORS
app.UseCors("AllowAll");

// 初始化数据库
using (var scope = app.Services.CreateScope())
{
    var dbContext = scope.ServiceProvider.GetRequiredService<BLSLDbContext>();
    dbContext.Database.EnsureCreated();
    
    // 检查是否已存在测试数据
    if (dbContext.MixingRecords.Count() < 500 || dbContext.FeedingRecords.Count() < 500 || dbContext.SemiProductMaterials.Count() < 500 || dbContext.Buckets.Count() < 500)
    {
        // 清除现有记录
        dbContext.MixingRecords.RemoveRange(dbContext.MixingRecords);
        dbContext.FeedingRecords.RemoveRange(dbContext.FeedingRecords);
        dbContext.SemiProductMaterials.RemoveRange(dbContext.SemiProductMaterials);
        dbContext.Buckets.RemoveRange(dbContext.Buckets);
        
        // 生成500条拌料记录测试数据
        var mixingRecords = new List<BLSLDev_api.Models.MixingRecord>();
        var feedingRecords = new List<BLSLDev_api.Models.FeedingRecord>();
        var semiProductMaterials = new List<BLSLDev_api.Models.SemiProductMaterial>();
        var buckets = new List<BLSLDev_api.Models.Bucket>();
        var random = new Random();
        var semiProductCodes = new[] { "SF001", "SF002", "SF003", "SF004", "SF005", "SF006", "SF007", "SF008", "SF009", "SF010" };
        var rawMaterialCodes = new[] { "RM001", "RM002", "RM003", "RM004", "RM005", "RM006", "RM007", "RM008", "RM009", "RM010" };
        var bucketCodes = new[] { "1#", "2#", "3#", "4#", "5#", "6#", "7#", "8#", "9#", "10#", "11#", "12#", "13#", "14#", "15#", "16#", "17#", "18#", "19#", "20#" };
        var validationResults = new[] { "验证成功", "验证失败" };

        // 生成物料记录
        for (int i = 1; i <= 500; i++)
        {
            semiProductMaterials.Add(new BLSLDev_api.Models.SemiProductMaterial
            {
                SemiProductCode = semiProductCodes[random.Next(semiProductCodes.Length)],
                SemiProductDesc = $"半成品{i}",
                RawMaterialCode = rawMaterialCodes[random.Next(rawMaterialCodes.Length)],
                RawMaterialDesc = $"原料{i}",
                Quantity = $"{random.Next(1, 10)}kg",
                CreatedAt = DateTime.Now,
                UpdatedAt = DateTime.Now
            });
        }

        // 生成桶料记录
        for (int i = 1; i <= 500; i++)
        {
            buckets.Add(new BLSLDev_api.Models.Bucket
            {
                Code = bucketCodes[i % bucketCodes.Length],
                RawMaterialCode = rawMaterialCodes[random.Next(rawMaterialCodes.Length)],
                RawMaterialDesc = $"原料{i}",
                Weight = (decimal)(random.Next(10, 50) + random.NextDouble()),
                Status = random.Next(2) == 0 ? "空闲" : "使用中",
                CreatedAt = DateTime.Now,
                UpdatedAt = DateTime.Now
            });
        }

        // 生成拌料记录
        for (int i = 1; i <= 500; i++)
        {
            mixingRecords.Add(new BLSLDev_api.Models.MixingRecord
            {
                SemiProductCode = semiProductCodes[random.Next(semiProductCodes.Length)],
                RawMaterialCode = rawMaterialCodes[random.Next(rawMaterialCodes.Length)],
                BucketCode = bucketCodes[random.Next(bucketCodes.Length)],
                Quantity = $"{random.Next(1, 10)}kg",
                OperationTime = DateTime.Now.AddMinutes(-random.Next(1, 10000))
            });
        }

        // 生成加料记录
        for (int i = 1; i <= 500; i++)
        {
            feedingRecords.Add(new BLSLDev_api.Models.FeedingRecord
            {
                RawMaterialCode = rawMaterialCodes[random.Next(rawMaterialCodes.Length)],
                BucketCode = bucketCodes[random.Next(bucketCodes.Length)],
                ValidationResult = validationResults[random.Next(validationResults.Length)],
                OperationTime = DateTime.Now.AddMinutes(-random.Next(1, 10000))
            });
        }

        // 添加测试数据
        dbContext.SemiProductMaterials.AddRange(semiProductMaterials);
        dbContext.Buckets.AddRange(buckets);
        dbContext.MixingRecords.AddRange(mixingRecords);
        dbContext.FeedingRecords.AddRange(feedingRecords);

        // 保存到数据库
        dbContext.SaveChanges();
    }
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
