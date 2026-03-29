using BLSLDev_api.Models;
using Microsoft.EntityFrameworkCore;

namespace BLSLDev_api.Data
{
    public class BLSLDbContext : DbContext
    {
        public BLSLDbContext(DbContextOptions<BLSLDbContext> options) : base(options)
        {}

        public DbSet<SemiProductMaterial> SemiProductMaterials { get; set; }
        public DbSet<Bucket> Buckets { get; set; }
        public DbSet<MixingRecord> MixingRecords { get; set; }
        public DbSet<FeedingRecord> FeedingRecords { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // 配置表名
            modelBuilder.Entity<SemiProductMaterial>().ToTable("SemiProductMaterials");
            modelBuilder.Entity<Bucket>().ToTable("Buckets");
            modelBuilder.Entity<MixingRecord>().ToTable("MixingRecords");
            modelBuilder.Entity<FeedingRecord>().ToTable("FeedingRecords");

            // 添加索引
            modelBuilder.Entity<SemiProductMaterial>()
                .HasIndex(s => s.SemiProductCode);
            
            modelBuilder.Entity<Bucket>()
                .HasIndex(b => b.Code);
            
            modelBuilder.Entity<Bucket>()
                .HasIndex(b => b.RawMaterialCode);
            
            modelBuilder.Entity<MixingRecord>()
                .HasIndex(m => m.OperationTime);
            
            modelBuilder.Entity<FeedingRecord>()
                .HasIndex(f => f.OperationTime);

            // 初始化数据
            modelBuilder.Entity<Bucket>().HasData(
                new Bucket { Id = 1, Code = "1#", RawMaterialCode = "RM001", RawMaterialDesc = "原料1", Weight = 25.5m, Status = "空闲", CreatedAt = DateTime.Now, UpdatedAt = DateTime.Now },
                new Bucket { Id = 2, Code = "2#", RawMaterialCode = "RM002", RawMaterialDesc = "原料2", Weight = 30.0m, Status = "空闲", CreatedAt = DateTime.Now, UpdatedAt = DateTime.Now },
                new Bucket { Id = 3, Code = "3#", RawMaterialCode = "RM003", RawMaterialDesc = "原料3", Weight = 20.0m, Status = "空闲", CreatedAt = DateTime.Now, UpdatedAt = DateTime.Now },
                new Bucket { Id = 4, Code = "4#", RawMaterialCode = "RM001", RawMaterialDesc = "原料1", Weight = 25.5m, Status = "空闲", CreatedAt = DateTime.Now, UpdatedAt = DateTime.Now },
                new Bucket { Id = 5, Code = "5#", RawMaterialCode = "RM002", RawMaterialDesc = "原料2", Weight = 30.0m, Status = "空闲", CreatedAt = DateTime.Now, UpdatedAt = DateTime.Now },
                new Bucket { Id = 6, Code = "8#", RawMaterialCode = "RM003", RawMaterialDesc = "原料3", Weight = 20.0m, Status = "空闲", CreatedAt = DateTime.Now, UpdatedAt = DateTime.Now }
            );

            modelBuilder.Entity<SemiProductMaterial>().HasData(
                new SemiProductMaterial { Id = 1, SemiProductCode = "SF001", SemiProductDesc = "半成品1", RawMaterialCode = "RM001", RawMaterialDesc = "原料1", Quantity = "3kg", CreatedAt = DateTime.Now, UpdatedAt = DateTime.Now },
                new SemiProductMaterial { Id = 2, SemiProductCode = "SF001", SemiProductDesc = "半成品1", RawMaterialCode = "RM002", RawMaterialDesc = "原料2", Quantity = "3kg", CreatedAt = DateTime.Now, UpdatedAt = DateTime.Now },
                new SemiProductMaterial { Id = 3, SemiProductCode = "SF002", SemiProductDesc = "半成品2", RawMaterialCode = "RM003", RawMaterialDesc = "原料3", Quantity = "3kg", CreatedAt = DateTime.Now, UpdatedAt = DateTime.Now }
            );

            // 生成500条拌料记录测试数据
            var mixingRecords = new List<MixingRecord>();
            var feedingRecords = new List<FeedingRecord>();
            var random = new Random();
            var semiProductCodes = new[] { "SF001", "SF002", "SF003", "SF004", "SF005" };
            var rawMaterialCodes = new[] { "RM001", "RM002", "RM003", "RM004", "RM005" };
            var bucketCodes = new[] { "1#", "2#", "3#", "4#", "5#", "8#" };
            var validationResults = new[] { "验证成功", "验证失败" };

            // 生成拌料记录
            for (int i = 1; i <= 500; i++)
            {
                mixingRecords.Add(new MixingRecord
                {
                    Id = i,
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
                feedingRecords.Add(new FeedingRecord
                {
                    Id = i,
                    RawMaterialCode = rawMaterialCodes[random.Next(rawMaterialCodes.Length)],
                    BucketCode = bucketCodes[random.Next(bucketCodes.Length)],
                    ValidationResult = validationResults[random.Next(validationResults.Length)],
                    OperationTime = DateTime.Now.AddMinutes(-random.Next(1, 10000))
                });
            }

            modelBuilder.Entity<MixingRecord>().HasData(mixingRecords);
            modelBuilder.Entity<FeedingRecord>().HasData(feedingRecords);
        }
    }
}
