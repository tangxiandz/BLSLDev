using System.ComponentModel.DataAnnotations;

namespace BLSLDev_api.Models
{
    public class SemiProductMaterial
    {
        [Key]
        public int Id { get; set; }
        
        [Required]
        public string SemiProductCode { get; set; }
        
        [Required]
        public string SemiProductDesc { get; set; }
        
        [Required]
        public string RawMaterialCode { get; set; }
        
        [Required]
        public string RawMaterialDesc { get; set; }
        
        public string Quantity { get; set; }
        
        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
    }
}
